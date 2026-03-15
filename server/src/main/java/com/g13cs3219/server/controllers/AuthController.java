package com.g13cs3219.server.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.g13cs3219.server.dto.AuthResponse;
import com.g13cs3219.server.dto.LoginRequest;
import com.g13cs3219.server.dto.User;
import com.g13cs3219.server.services.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping(path = "/register")
    public ResponseEntity<Long> register(@RequestBody User user) {
        Long userId = authService.register(user);
        return ResponseEntity.created(null).body(userId);
    }

    @PostMapping(path = "/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok(authResponse);
    }
}
