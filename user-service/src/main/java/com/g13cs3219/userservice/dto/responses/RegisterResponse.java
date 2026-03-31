package com.g13cs3219.userservice.dto.responses;

import lombok.*;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class RegisterResponse {
    private Long id;

    public static RegisterResponse buildResponse(Long userId) {
        return RegisterResponse.builder().id(userId).build();
    }
}
