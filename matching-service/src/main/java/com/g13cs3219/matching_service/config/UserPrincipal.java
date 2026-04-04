package com.g13cs3219.matching_service.config;

import java.security.Principal;

public class UserPrincipal implements Principal {
    private final String userId;

    public UserPrincipal(String userId) {
        this.userId = userId;
    }

    @Override
    public String getName() {
        return userId;
    }
}
