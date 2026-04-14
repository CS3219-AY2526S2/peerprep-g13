package com.g13cs3219.matching_service.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MatchResult {
    private Long userId1;
    private Long userId2;
    private String topic;
    private String difficulty;
    private QuestionResponse question;
    private String roomId;
    private Long questionId;
}
