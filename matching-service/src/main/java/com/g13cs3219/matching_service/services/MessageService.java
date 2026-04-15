package com.g13cs3219.matching_service.services;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatusCode;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.g13cs3219.matching_service.dto.requests.QuestionRequest;
import com.g13cs3219.matching_service.dto.responses.MatchResult;

@Service
public class MessageService {

    @Value("${question-service.url}")
    private String questionServiceURL;

    private static final Logger log = LoggerFactory.getLogger(MessageService.class.getName());

    private final SimpMessagingTemplate messagingTemplate;
    private final WebClient.Builder webClientBuilder;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public MessageService(SimpMessagingTemplate messagingTemplate,
                          WebClient.Builder webClientBuilder,
                          RedisTemplate<String, String> redisTemplate,
                          ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
        this.webClientBuilder = webClientBuilder;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Send a timeout message to the user when they have been waiting for too long.
     *
     * @param userId The ID of the user to send the message to.
     */
    public void sendTimeoutMessage(String userId) {
        log.info("Sending timeout message to user: {}", userId);
        messagingTemplate.convertAndSendToUser(
            userId,
            "/queue/match",
            "No match found"
        );
    }

    /**
     * Send a match found message to both users when a match is found.
     * Fetches the question from question-service, writes the room entry to Redis,
     * then notifies both users via STOMP.
     *
     * @param match The match result containing the user IDs and question criteria.
     */
    public void sendMatchFoundMessage(MatchResult match) {
        log.info("Sending match found message to users: {} and {}", match.getUserId1(), match.getUserId2());

        QuestionRequest request = QuestionRequest.buildQuestionRequest(match.getTopic(), match.getDifficulty());
        Long questionId = fetchQuestionId(request);

        match.setQuestionId(questionId);
        log.info("Assigned questionId {} to room {}", questionId, match.getRoomId());

        // Look up participant emails from the pool's email index
        String email1 = redisTemplate.opsForValue().get("user-email:" + match.getUserId1());
        String email2 = redisTemplate.opsForValue().get("user-email:" + match.getUserId2());

        // Write room entry to Redis once, with all data resolved
        registerRoom(match, email1, email2);

        messagingTemplate.convertAndSendToUser(
            match.getUserId1() + "",
            "/queue/match",
            match
        );
        messagingTemplate.convertAndSendToUser(
            match.getUserId2() + "",
            "/queue/match",
            match
        );
    }

    /**
     * Send a match cancelled message to the user when they cancel their match request.
     *
     * @param userId The ID of the user to send the message to.
     */
    public void sendCancelMessage(String userId) {
        log.info("Sending match cancelled message to user: {}", userId);
        messagingTemplate.convertAndSendToUser(
            userId,
            "/queue/match",
            "Match cancelled"
        );
    }

    private void registerRoom(MatchResult match, String email1, String email2) {
        Map<String, Object> roomData = Map.of(
            "userId1",    match.getUserId1(),
            "userId2",    match.getUserId2(),
            "email1",     email1 != null ? email1 : "",
            "email2",     email2 != null ? email2 : "",
            "questionId", match.getQuestionId(),
            "topic",      match.getTopic(),
            "difficulty", match.getDifficulty()
        );
        try {
            String json = objectMapper.writeValueAsString(roomData);
            redisTemplate.opsForValue().set(
                "room:" + match.getRoomId(),
                json,
                3600, TimeUnit.SECONDS
            );
            log.info("Registered room {} in Redis for users {} and {}", match.getRoomId(), match.getUserId1(), match.getUserId2());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize room data for room {}: {}", match.getRoomId(), e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private Long fetchQuestionId(QuestionRequest request) {
        Map<String, Object> response = webClientBuilder
                .build()
                .post()
                .uri(questionServiceURL + "/questions/match/id")
                .header("X-Internal-Request", "true")
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, res ->
                        res.bodyToMono(String.class)
                                .map(body -> new RuntimeException("Error: " + body))
                )
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(3))
                .block();

        if (response == null || !response.containsKey("questionId")) {
            throw new RuntimeException("Empty or invalid response from question service");
        }
        return ((Number) response.get("questionId")).longValue();
    }
}
