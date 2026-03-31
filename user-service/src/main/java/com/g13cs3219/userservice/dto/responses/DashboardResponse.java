package com.g13cs3219.userservice.dto.responses;

import lombok.*;
import java.util.List;
import java.util.Map;

@Data @Builder
public class DashboardResponse {
    private UserDashboardResponse user;
    private List<HistoryDashboardResponse> history;
    private Map<String, Object> metrics;

    public static DashboardResponse build(UserDashboardResponse user, List<HistoryDashboardResponse> history) {
        long totalSolved = history.stream().map(HistoryDashboardResponse::getQuestionId).distinct().count();
        return DashboardResponse.builder().user(user).history(history).metrics(Map.of("totalSolved", totalSolved)).build();
    }
}
