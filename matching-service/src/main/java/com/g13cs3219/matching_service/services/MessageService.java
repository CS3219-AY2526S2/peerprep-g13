package com.g13cs3219.matching_service.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.g13cs3219.matching_service.dto.requests.QuestionRequest;
import com.g13cs3219.matching_service.dto.responses.MatchResult;
import com.g13cs3219.matching_service.dto.responses.QuestionResponse;
import com.g13cs3219.matching_service.dto.responses.QuestionResponseWrapper;

import lombok.AllArgsConstructor;

@Service
public class MessageService {

    @Value("${question-service.url}")
    private String questionServiceURL;

    private static final Logger log = LoggerFactory.getLogger(MessageService.class.getName());

    private final SimpMessagingTemplate messagingTemplate;
    private final WebClient.Builder webClientBuilder;

    public MessageService(SimpMessagingTemplate messagingTemplate, WebClient.Builder webClientBuilder) {
        this.messagingTemplate = messagingTemplate;
        this.webClientBuilder = webClientBuilder;
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
     *
     * @param match The match result containing the user IDs and question ID.
     */
    public void sendMatchFoundMessage(MatchResult match) {
        log.info("Sending match found message to users: {} and {}", match.getUserId1(), match.getUserId2());

        QuestionRequest request = QuestionRequest.buildQuestionRequest(match.getTopic(), match.getDifficulty());
        QuestionResponse randomQuestion = getRandomQuestion(request).getQuestion();

        match.setQuestion(randomQuestion);
        log.info("Random question found: {}", randomQuestion);

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

    private QuestionResponseWrapper getRandomQuestion(QuestionRequest request) {
        return webClientBuilder
                .build()
                .post()
                .uri(questionServiceURL + "/questions/match")
                .header("X-Internal-Request", "true")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(QuestionResponseWrapper.class)
                .block();
    }
}
