package com.g13cs3219.matching_service.services;

import  org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.g13cs3219.matching_service.dto.responses.MatchResult;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class MessageService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Send a timeout message to the user when they have been waiting for too long.
     *
     * @param userId The ID of the user to send the message to.
     */
    public void sendTimeoutMessage(String userId) {
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
        messagingTemplate.convertAndSendToUser(
            userId,
            "/queue/match",
            "Match cancelled"
        );
    }
}
