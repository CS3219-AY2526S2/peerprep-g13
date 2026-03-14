package com.g13cs3219.server.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class QuestionRequest {
    private String title;
    private String topic;
    private String difficulty;
    private String prompt;
    private List<String> example    = new ArrayList<>();
    private List<String> constraints = new ArrayList<>();
    private List<String> imageUrls  = new ArrayList<>();
}