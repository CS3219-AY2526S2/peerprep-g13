package com.g13cs3219.matching_service.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.g13cs3219.matching_service.dto.requests.JoinRequest;
import com.g13cs3219.matching_service.repositories.MatchingPool;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ConsumerService {

    private static final Logger log = LoggerFactory.getLogger(ConsumerService.class);
    private final MatchingPool matchingPool;
    private final MessageService messageService;

    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void processRequest(JoinRequest request) {
        if (request.getType().equals("cancel")) {
            log.info("Received cancel request for user: {}", request.getUserId());
            matchingPool.removeUser(request);
        } else if (request.getType().equals("loose-match")) {
            log.info("Received loose match request for user: {}", request.getUserId());
            matchingPool.removeUser(request);
            handleJoinRequest(request);
        } else {
            log.info("Received match request for user: {}", request.getUserId());
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
                            log.info("No match found immediately for user: {}, adding to pool", request.getUserId());
                            matchingPool.addUser(request);
                        });
    }
}
