package com.g13cs3219.matching_service.controllers;

import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping("/join")
    public void enqueue(@RequestBody JoinRequest request) {
        producerService.sendJoinRequest(request);
    }

    @GetMapping("/join/loose")
    public void enqueueLoose(@RequestBody JoinRequest request) {
        producerService.sendAdvancedJoinRequest(request);
    }

    @GetMapping("/leave")
    public void dequeue(@RequestBody JoinRequest request) {
        producerService.sendCancelRequest(request);
    }
}
