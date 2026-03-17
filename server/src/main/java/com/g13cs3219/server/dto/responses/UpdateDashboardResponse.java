package com.g13cs3219.server.dto.responses;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateDashboardResponse {
    private String message;

    public static UpdateDashboardResponse success() {
        return UpdateDashboardResponse.builder()
                .message("Update dashboard successfully")
                .build();
    }
}