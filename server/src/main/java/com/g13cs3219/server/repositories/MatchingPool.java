package com.g13cs3219.server.repositories;

import java.util.Optional;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import com.g13cs3219.server.dto.requests.JoinRequest;
import com.g13cs3219.server.services.MessageService;

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
    public Optional<String> findMatch(String topic, String difficulty, String type) {
        String key = buildKey(topic, difficulty);

        // First try to find an exact match
        Optional<String> match = findExactMatch(key);
        if (match.isPresent()) {
            return match;
        }

        // If no exact match, try to find a match with the same difficulty
        match = findSameDifficultyMatch(difficulty);
        if (type.equals("match") || match.isPresent()) {
            return match;
        }

        // If still no match, try to find a match with the same topic but lower difficulty
        match = findExactMatch(getLowerDifficulty(difficulty));
        if (match.isPresent()) {
            return match;
        }

        // If still no match, try to find a match with the same topic but higher difficulty
        match = findExactMatch(getHigherDifficulty(difficulty));
        if (match.isPresent()) {
            return match;
        }

        // If still no match, try to find a match with the different topic but lower difficulty
        match = findSameDifficultyMatch(getLowerDifficulty(difficulty));
        if (match.isPresent()) {
            return match;
        }

        // If still no match, try to find a match with the different topic but higher difficulty
        match = findSameDifficultyMatch(getHigherDifficulty(difficulty));
        return match;
    }

    /**
     * Handle timeouts for users in the matching pool by removing users who have been waiting for more than 30 seconds.
     *
     * @param currentTime the current time in milliseconds to compare against the enqueue times of users in the
     *                    matching pool
     */
    public void handleTimeouts(double currentTime) {
        Set<ZSetOperations.TypedTuple<String>> users = redisTemplate
                .opsForZSet()
                .rangeWithScores("*", (long) Double.NEGATIVE_INFINITY, (long) (currentTime - 30000));
        // 30 seconds timeout

        if (users != null) {
            for (ZSetOperations.TypedTuple<String> user : users) {
                String userId = user.getValue();
                messageService.sendTimeoutMessage(userId);
                redisTemplate.opsForZSet().remove("*", userId);
            }
        }
    }

    /**
     * Find an exact match for the specified key.
     *
     * @param key the key to search for an exact match
     * @return an Optional containing the matched user ID if a match is found, or an empty Optional if no match is found
     */
    private Optional<String> findExactMatch(String key) {
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
            return Optional.of(match);
        }
        return Optional.empty();
    }

    /**
     * Find a match with the same difficulty level for the specified difficulty.
     *
     * @param difficulty the difficulty level to search for a match
     * @return an Optional containing the matched user ID if a match is found, or an empty Optional if no match is found
     */
    private Optional<String> findSameDifficultyMatch(String difficulty) {
        Set<String> keys = redisTemplate.keys("*" + difficulty + "*");
        if (keys == null || keys.isEmpty()) {
            return Optional.empty();
        }

        String key = keys.iterator().next();
        return findExactMatch(key);
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
