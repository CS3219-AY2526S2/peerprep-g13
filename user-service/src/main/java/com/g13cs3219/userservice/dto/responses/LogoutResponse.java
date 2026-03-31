package com.g13cs3219.userservice.dto.responses;

import lombok.*;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class LogoutResponse {
    private String message;

    public static LogoutResponse buildResponse() {
        return LogoutResponse.builder().message("Logout successful.").build();
    }
}
