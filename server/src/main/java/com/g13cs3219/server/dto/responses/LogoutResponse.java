package com.g13cs3219.server.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LogoutResponse {
    private String message;

    public static LogoutResponse buildResponse() {
        return LogoutResponse.builder()
                .message("Logout successful.")
                .build();
    }
}
