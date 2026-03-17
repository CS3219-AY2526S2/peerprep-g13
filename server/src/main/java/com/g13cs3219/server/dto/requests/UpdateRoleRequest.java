package com.g13cs3219.server.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateRoleRequest {
    private String newRole;

    public static void validateRequest(UpdateRoleRequest request) {
        if (request.getNewRole() == null) {
            throw new IllegalArgumentException("new role is required");
        }
    }
}
