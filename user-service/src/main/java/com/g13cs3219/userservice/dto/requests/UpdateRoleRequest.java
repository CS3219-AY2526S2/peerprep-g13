package com.g13cs3219.userservice.dto.requests;

import lombok.*;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class UpdateRoleRequest {
    private String newRole;

    public static void validateRequest(UpdateRoleRequest request) {
        if (request.getNewRole() == null) throw new IllegalArgumentException("new role is required");
    }
}
