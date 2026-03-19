package com.g13cs3219.server.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdatePasswordResponse {
    private String message;

    public static UpdatePasswordResponse buildResponse() {
        return UpdatePasswordResponse.builder()
                .message("Password updated successfully.")
                .build();
    }
}
