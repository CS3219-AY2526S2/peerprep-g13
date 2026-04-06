package com.g13cs3219.matching_service.dto.responses;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class QuestionResponse {
    private Long questionId;
    private String title;
    private String topic;
    private String difficulty;
    private String prompt;
    private List<String> example;
    private List<String> constraints;
    private List<String> imageUrls;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;
}
