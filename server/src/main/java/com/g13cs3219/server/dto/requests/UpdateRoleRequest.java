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
    private Long adminId;
    private String password;
    private String newRole;

    public static void validateRequest(UpdateRoleRequest request) {
        if (request.getAdminId() == null) {
            throw new IllegalArgumentException("Admin ID is required");
        }
        if (request.getPassword() == null) {
            throw new IllegalArgumentException("Password is required");
        }
        if (request.getNewRole() == null) {
            throw new IllegalArgumentException("new role is required");
        }
    }
}
