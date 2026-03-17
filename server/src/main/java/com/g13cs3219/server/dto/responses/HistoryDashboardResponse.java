package com.g13cs3219.server.dto.responses;

import com.g13cs3219.server.model.History;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
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