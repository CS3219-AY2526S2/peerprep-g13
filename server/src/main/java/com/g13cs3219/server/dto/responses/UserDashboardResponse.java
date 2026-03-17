package com.g13cs3219.server.dto.responses;

import com.g13cs3219.server.model.User;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class UserDashboardResponse {
    private Long userId;
    private String username;
    private String role;
    private String name;
    private String avatarUrl;
    private String preferredLanguage;
    private List<String> preferredTopic;

    public static UserDashboardResponse from(User user) {
        return UserDashboardResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .role(user.getRole().name().toLowerCase())
                .name(user.getName())
                .avatarUrl(user.getAvatarUrl())
                .preferredLanguage(user.getPreferredLanguage())
                .preferredTopic(user.getPreferredTopic())
                .build();
    }
}