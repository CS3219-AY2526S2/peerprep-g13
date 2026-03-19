package com.g13cs3219.server.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import com.g13cs3219.server.dto.responses.AuthResponse;
import com.g13cs3219.server.dto.responses.LoginResponse;
import com.g13cs3219.server.dto.requests.LoginRequest;
import com.g13cs3219.server.dto.requests.RegisterRequest;
import com.g13cs3219.server.dto.responses.LogoutResponse;
import com.g13cs3219.server.dto.responses.RegisterResponse;
import com.g13cs3219.server.services.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping(path = "/user/auth/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity.created(null).body(Map.of("data", response));
    }

    @PostMapping(path = "/user/auth/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(Map.of("data", response));
    }

    @PostMapping(path = "/user/auth/logout")
    public ResponseEntity<Map<String, Object>> logout(Authentication authentication) {
        LogoutResponse response = authService.logout(authentication);
        return ResponseEntity.ok(Map.of("message", "Logout successful"));
    }

    @GetMapping(path = "/user/auth")
    public ResponseEntity<Map<String, Object>> getAuth(Authentication authentication) {
        AuthResponse authResponse = authService.getAuth(authentication);
        return ResponseEntity.ok(Map.of("data", authResponse));
    }
}
