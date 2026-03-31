package com.g13cs3219.userservice.dto.requests;

import lombok.*;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class UpdatePasswordRequest {
    private String currentPassword;
    private String newPassword;
}
