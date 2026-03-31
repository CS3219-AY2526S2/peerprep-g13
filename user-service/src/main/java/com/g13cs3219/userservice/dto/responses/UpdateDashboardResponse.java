package com.g13cs3219.userservice.dto.responses;

import lombok.*;

@Data @Builder
public class UpdateDashboardResponse {
    private String message;

    public static UpdateDashboardResponse success() {
        return UpdateDashboardResponse.builder().message("Update dashboard successfully").build();
    }
}
