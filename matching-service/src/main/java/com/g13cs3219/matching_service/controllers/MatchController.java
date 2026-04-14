package com.g13cs3219.matching_service.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.g13cs3219.matching_service.dto.requests.JoinRequest;
import com.g13cs3219.matching_service.services.ProducerService;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/match")
public class MatchController {

    private final ProducerService producerService;

    @PostMapping("/join")
    public void enqueue(@RequestBody JoinRequest request, Authentication authentication) {
        request.setEmail(authentication.getName());
        producerService.sendJoinRequest(request);
    }

    @PostMapping("/join/loose")
    public void enqueueLoose(@RequestBody JoinRequest request, Authentication authentication) {
        request.setEmail(authentication.getName());
        producerService.sendAdvancedJoinRequest(request);
    }

    @PostMapping("/leave")
    public void dequeue(@RequestBody JoinRequest request, Authentication authentication) {
        request.setEmail(authentication.getName());
        producerService.sendCancelRequest(request);
    }
}
