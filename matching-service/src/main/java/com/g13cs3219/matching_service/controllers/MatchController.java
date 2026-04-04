package com.g13cs3219.matching_service.controllers;

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

    private ProducerService producerService;

    @PostMapping("/join")
    public void enqueue(@RequestBody JoinRequest request) {
        producerService.sendJoinRequest(request);
    }

    @PostMapping("/join/loose")
    public void enqueueLoose(@RequestBody JoinRequest request) {
        producerService.sendAdvancedJoinRequest(request);
    }

    @PostMapping("/leave")
    public void dequeue(@RequestBody JoinRequest request) {
        producerService.sendCancelRequest(request);
    }
}
