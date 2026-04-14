package com.g13cs3219.matching_service.dto.requests;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QuestionRequest {
    private String topic;
    private String difficulty;

    public static QuestionRequest buildQuestionRequest(String topic, String difficulty) {
        return QuestionRequest
                .builder()
                .topic(topic)
                .difficulty(difficulty)
                .build();
    }
}
