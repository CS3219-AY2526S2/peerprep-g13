package com.g13cs3219.questionservice.dto.responses;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TopicResponse {
    private List<String> topics;

    public static TopicResponse from(List<String> topics) {
        return TopicResponse.builder().topics(topics).build();
    }
}
