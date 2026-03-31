package com.g13cs3219.userservice.dto.responses;

import com.g13cs3219.userservice.model.History;
import lombok.*;
import java.time.LocalDateTime;

@Data @Builder
public class HistoryDashboardResponse {
    private Long questionId;
    private LocalDateTime finishedDate;

    public static HistoryDashboardResponse from(History history) {
        return HistoryDashboardResponse.builder()
                .questionId(history.getQuestionId())
                .finishedDate(history.getFinishedDate())
                .build();
    }
}
