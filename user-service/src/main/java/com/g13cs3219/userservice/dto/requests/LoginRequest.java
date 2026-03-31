package com.g13cs3219.userservice.dto.requests;

import lombok.*;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class LoginRequest {
    private String email;
    private String password;

    public static void validate(LoginRequest request) {
        if (request == null) throw new IllegalArgumentException("Request body is required");
        if (request.getEmail() == null || request.getEmail().isBlank()) throw new IllegalArgumentException("Email is required");
        if (request.getPassword() == null || request.getPassword().isBlank()) throw new IllegalArgumentException("Password is required");
    }
}
