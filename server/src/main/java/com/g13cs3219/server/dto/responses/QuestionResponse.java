package com.g13cs3219.server.dto.responses;

import com.g13cs3219.server.model.Question;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

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

    public static QuestionResponse from(Question q) {
        QuestionResponse dto = new QuestionResponse();
        dto.setQuestionId(q.getQuestionId());
        dto.setTitle(q.getTitle());
        dto.setTopic(q.getTopic());
        dto.setDifficulty(q.getDifficulty());
        dto.setPrompt(q.getPrompt());
        dto.setExample(q.getExample());
        dto.setConstraints(q.getConstraints());
        dto.setImageUrls(q.getImageUrls());
        dto.setCreatedBy(q.getCreatedBy());
        dto.setCreatedAt(q.getCreatedAt());
        dto.setUpdatedBy(q.getUpdatedBy());
        dto.setUpdatedAt(q.getUpdatedAt());
        return dto;
    }
}
