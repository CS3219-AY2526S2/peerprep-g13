package com.g13cs3219.matching_service.repositories;

import java.util.Optional;
import java.util.Set;

import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import com.g13cs3219.matching_service.dto.requests.JoinRequest;
import com.g13cs3219.matching_service.dto.responses.MatchResult;
import com.g13cs3219.matching_service.services.MessageService;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class MatchingPool {

    private final RedisTemplate<String, String> redisTemplate;
    private final MessageService messageService;

    /**
     * Add a user to the matching pool based on the provided join request.
     *
     * @param request the join request containing the topic and difficulty for matching
     */
    public void addUser(JoinRequest request) {
        String key = buildKey(request.getTopic(), request.getDifficulty());

        redisTemplate.opsForZSet().add(
                key,
                String.valueOf(request.getUserId()),
                System.currentTimeMillis()
        );
    }

    /**
     * Remove a user from the matching pool based on the provided join request.
     *
     * @param request the join request containing the topic and difficulty for matching
     */
    public void removeUser(JoinRequest request) {
        String key = buildKey(request.getTopic(), request.getDifficulty());

        messageService.sendCancelMessage(String.valueOf(request.getUserId()));
        redisTemplate.opsForZSet().remove(key, String.valueOf(request.getUserId()));
    }

    /**
     * Find a match for a user based on the specified topic and difficulty.
     *
     * @param topic      the topic the user wants to be matched on
     * @param difficulty the difficulty level the user wants to be matched on
     * @return an Optional containing the matched user ID if a match is found, or an empty Optional if no match is found
     */
    public Optional<MatchResult> findMatch(int userId, String topic, String difficulty, String type) {
        String key = buildKey(topic, difficulty);

        // First try to find an exact match
        Optional<MatchResult> match = findExactMatch(userId, key);
        if (match.isPresent()) {
            return match;
        }

        // If no exact match, try to find a match with the same difficulty
        match = findSameDifficultyMatch(userId, difficulty);
        if (type.equals("match") || match.isPresent()) {
            return match;
        }


        // If still no match, try to find a match with the same topic but lower difficulty
        String lowDiffKey = buildKey(topic, getLowerDifficulty(difficulty));
        match = findExactMatch(userId, lowDiffKey);
        if (match.isPresent()) {
            return match;
        }

        // If still no match, try to find a match with the same topic but higher difficulty
        String highDiffKey = buildKey(topic, getHigherDifficulty(difficulty));
        match = findExactMatch(userId, highDiffKey);
        if (match.isPresent()) {
            return match;
        }

        // If still no match, try to find a match with the different topic but lower difficulty
        match = findSameDifficultyMatch(userId, getLowerDifficulty(difficulty));
        if (match.isPresent()) {
            return match;
        }

        // If still no match, try to find a match with the different topic but higher difficulty
        match = findSameDifficultyMatch(userId, getHigherDifficulty(difficulty));
        return match;
    }

    /**
     * Handle timeouts for users in the matching pool by removing users who have been waiting for more than 30 seconds.
     *
     * @param currentTime the current time in milliseconds to compare against the enqueue times of users in the
     *                    matching pool
     */
    public void handleTimeouts(double currentTime) {
        ScanOptions options = ScanOptions.scanOptions().match("queue:*").count(100).build();
        Cursor<byte[]> cursor = redisTemplate.getConnectionFactory().getConnection().scan(options);
        while (cursor.hasNext()) {
            String key = new String(cursor.next());
            Set<ZSetOperations.TypedTuple<String>> timedOutUsers = redisTemplate
                    .opsForZSet()
                    .rangeByScoreWithScores(key, 0, currentTime - 30000);

            if (timedOutUsers != null) {
                for (ZSetOperations.TypedTuple<String> user : timedOutUsers) {
                    String userId = user.getValue();

                    messageService.sendTimeoutMessage(userId);
                    redisTemplate.opsForZSet().remove(key, userId);
                }
            }
        }
        cursor.close();
    }

    private Optional<MatchResult> findExactMatch(int userId, String key) {
        if (key == null) {
            return Optional.empty();
        }

        Set<String> candidates = redisTemplate.opsForZSet().range(key, 0, 0);
        if (candidates == null || candidates.isEmpty()) {
            return Optional.empty();
        }

        String match = candidates.iterator().next();
        if (match != null) {
            redisTemplate.opsForZSet().remove(key, match);
            return Optional.of(MatchResult.builder()
                    .userId1(userId)
                    .userId2(Integer.parseInt(match))
                    .topic(key.split(":")[1])
                    .difficulty(key.split(":")[2])
                    .build()
            );
        }
        return Optional.empty();
    }
    
    private Optional<MatchResult> findSameDifficultyMatch(int userId, String difficulty) {
        ScanOptions options = ScanOptions.scanOptions().match("*" + difficulty + "*").count(100).build();
        Cursor<byte[]> cursor = redisTemplate.getConnectionFactory().getConnection().scan(options);
        while (cursor.hasNext()) {
            String key = new String(cursor.next());
            return findExactMatch(userId, key);
        }
        cursor.close();
        return Optional.empty();
    }

    private String getHigherDifficulty(String difficulty) {
        if (difficulty.equals("easy")) {
            return "medium";
        } else if (difficulty.equals("medium")) {
            return "hard";
        }
        return null;
    }

    private String getLowerDifficulty(String difficulty) {
        if (difficulty.equals("medium")) {
            return "easy";
        } else if (difficulty.equals("hard")) {
            return "medium";
        }
        return null;
    }

    private String buildKey(String topic, String difficulty) {
        return "queue:" + topic + ":" + difficulty;
    }
}
