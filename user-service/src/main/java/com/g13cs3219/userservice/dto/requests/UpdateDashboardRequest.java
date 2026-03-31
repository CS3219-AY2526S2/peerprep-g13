package com.g13cs3219.userservice.dto.requests;

import lombok.Data;
import java.util.List;

@Data
public class UpdateDashboardRequest {
    private String username;
    private String name;
    private String avatarUrl;
    private String preferredLanguage;
    private List<String> preferredTopic;
}
