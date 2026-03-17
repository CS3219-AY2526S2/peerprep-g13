package com.g13cs3219.server.dto.responses;

import com.g13cs3219.server.model.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponse {
    private Long userId;
    private String email;
    private Role role;

    public static AuthResponse buildResponse(Long userId, String email, Role role) {
        return AuthResponse.builder()
                .userId(userId)
                .email(email)
                .role(role)
                .build();
    }
}
