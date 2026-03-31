package com.g13cs3219.userservice.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import com.g13cs3219.userservice.dto.responses.AuthResponse;
import com.g13cs3219.userservice.dto.responses.LoginResponse;
import com.g13cs3219.userservice.dto.requests.LoginRequest;
import com.g13cs3219.userservice.dto.requests.RegisterRequest;
import com.g13cs3219.userservice.dto.responses.LogoutResponse;
import com.g13cs3219.userservice.dto.responses.RegisterResponse;
import com.g13cs3219.userservice.services.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/user/auth/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity.created(null).body(Map.of("data", response));
    }

    @PostMapping("/user/auth/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(Map.of("data", response));
    }

    @PostMapping("/user/auth/logout")
    public ResponseEntity<Map<String, Object>> logout(Authentication authentication) {
        authService.logout(authentication);
        return ResponseEntity.ok(Map.of("message", "Logout successful"));
    }

    @GetMapping("/user/auth")
    public ResponseEntity<Map<String, Object>> getAuth(Authentication authentication) {
        AuthResponse authResponse = authService.getAuth(authentication);
        return ResponseEntity.ok(Map.of("data", authResponse));
    }
}
