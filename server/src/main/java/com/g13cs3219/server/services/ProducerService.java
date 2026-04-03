package com.g13cs3219.server.services;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.g13cs3219.server.dto.requests.JoinRequest;
import com.g13cs3219.server.repositories.MatchingPool;

@Service
@EnableScheduling
public class ProducerService {

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    private final RabbitTemplate rabbitTemplate;
    private final MatchingPool matchingPool;

    public ProducerService(RabbitTemplate rabbitTemplate, MatchingPool matchingPool) {
        this.rabbitTemplate = rabbitTemplate;
        this.matchingPool = matchingPool;
    }

    @Scheduled(fixedRate = 1000) // Run every second
    public void processQueue() {
        matchingPool.handleTimeouts(System.currentTimeMillis());
    }

    public void sendJoinRequest(JoinRequest request) {
        rabbitTemplate.convertAndSend(exchangeName, routingKey, request);
    }

    public void sendAdvancedJoinRequest(JoinRequest request) {
        request.setType("loose_match");
        rabbitTemplate.convertAndSend(exchangeName, routingKey, request);
    }

    public void sendCancelRequest(JoinRequest request) {
        request.setType("cancel");
        rabbitTemplate.convertAndSend(exchangeName, routingKey, request);
    }
}
