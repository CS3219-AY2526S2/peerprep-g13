package com.g13cs3219.userservice.dto.responses;

import com.g13cs3219.userservice.model.Role;
import lombok.*;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class UpdateRoleResponse {
    private String message;
    private String newRole;

    public static UpdateRoleResponse buildResponse(Role role) {
        return UpdateRoleResponse.builder().message("User role updated successfully.").newRole(role.getId()).build();
    }
}
