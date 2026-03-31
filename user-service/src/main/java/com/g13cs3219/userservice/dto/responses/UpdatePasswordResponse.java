package com.g13cs3219.userservice.dto.responses;

import lombok.*;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class UpdatePasswordResponse {
    private String message;

    public static UpdatePasswordResponse buildResponse() {
        return UpdatePasswordResponse.builder().message("Password updated successfully.").build();
    }
}
