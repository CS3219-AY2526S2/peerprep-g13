package com.g13cs3219.server.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MatchResult {
    private int userId1;
    private int userId2;
    private String matchInfo;
}
