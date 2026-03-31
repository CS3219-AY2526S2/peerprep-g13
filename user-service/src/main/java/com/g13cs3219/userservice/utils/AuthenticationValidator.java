package com.g13cs3219.userservice.utils;

import org.springframework.security.core.Authentication;
import com.g13cs3219.userservice.model.User;

public class AuthenticationValidator {

    private AuthenticationValidator() {}

    public static void validateAuthentication(Authentication authentication) {
        if (authentication == null) {
            throw new IllegalStateException("Authentication is not available");
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            String principalType = (principal != null) ? principal.getClass().getName() : "null";
            throw new IllegalStateException("Unexpected principal type: " + principalType);
        }
    }
}
