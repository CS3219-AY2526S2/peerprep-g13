package com.g13cs3219.server.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResponse {
    private String message;
    private String accessToken;
    private Long userId;

    public static LoginResponse buildResponse(String accessToken, Long userId) {
        return LoginResponse.builder()
                .message("Login successful.")
                .accessToken(accessToken)
                .userId(userId)
                .build();
    }
}
