package com.g13cs3219.server.dto.responses;

import com.g13cs3219.server.model.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateRoleResponse {
    private String message;
    private String newRole;

    public static UpdateRoleResponse buildResponse(Role role) {
        return UpdateRoleResponse.builder()
                .message("User role updated successfully.")
                .newRole(role.toString())
                .build();
    }
}
