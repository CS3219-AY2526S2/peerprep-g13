package com.g13cs3219.matching_service.dto.requests;

import lombok.Data;

@Data
public class JoinRequest {
    private String type = "match"; // "match" or "loose-match" or "cancel"
    private int userId;
    private String topic;
    private String difficulty;
}
