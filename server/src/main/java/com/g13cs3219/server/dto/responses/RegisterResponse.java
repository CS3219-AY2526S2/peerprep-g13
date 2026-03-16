package com.g13cs3219.server.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterResponse {
    private Long id;

    public static RegisterResponse buildResponse(Long userId) {
        return RegisterResponse.builder()
                .id(userId)
                .build();
    }
}
