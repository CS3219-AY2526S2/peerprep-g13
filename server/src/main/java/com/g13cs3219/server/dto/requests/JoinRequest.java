package com.g13cs3219.server.dto.requests;

import lombok.Data;

@Data
public class JoinRequest {
    private String type = "match"; // "match" or "loose_match" or "cancel"
    private int userId;
    private String topic;
    private String difficulty;
}
