package com.g13cs3219.server.dto.requests;

import lombok.Data;

@Data
public class MatchRequest {
    private String topic;
    private String difficulty;
}