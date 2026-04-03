package com.g13cs3219.matching_service.services;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.g13cs3219.matching_service.dto.requests.JoinRequest;
import com.g13cs3219.matching_service.repositories.MatchingPool;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ConsumerService {

    private final MatchingPool matchingPool;
    private final MessageService messageService;

    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void processRequest(JoinRequest request) {
        if (request.getType().equals("cancel")) {
            matchingPool.removeUser(request);
        } else if (request.getType().equals("loose-match")) {
            matchingPool.removeUser(request);
            handleJoinRequest(request);
        } else {
            handleJoinRequest(request);
        }
    }

    /**
     * Handle a join request by attempting to find a match for the user. If a match is found, send a match found message
     * to both users. If no match is found, add the user to the matching pool.
     *
     * @param request the join request containing the user's matching preferences
     */
    private void handleJoinRequest(JoinRequest request) {
        matchingPool
                .findMatch(request.getUserId(), request.getTopic(), request.getDifficulty(), request.getType())
                .ifPresentOrElse(
                        messageService::sendMatchFoundMessage,
                        () -> {
                            matchingPool.addUser(request);
                        });
    }
}
