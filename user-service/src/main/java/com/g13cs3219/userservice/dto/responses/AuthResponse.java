package com.g13cs3219.userservice.dto.responses;

import com.g13cs3219.userservice.model.Role;
import lombok.*;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class AuthResponse {
    private Long userId;
    private String email;
    private Role role;

    public static AuthResponse buildResponse(Long userId, String email, Role role) {
        return AuthResponse.builder().userId(userId).email(email).role(role).build();
    }
}
