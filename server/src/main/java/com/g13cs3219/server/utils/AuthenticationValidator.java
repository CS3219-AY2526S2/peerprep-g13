package com.g13cs3219.server.utils;

import org.springframework.security.core.Authentication;

import com.g13cs3219.server.model.User;

/**
 * Utility class for validating authentication objects.
 * This class provides static methods to validate Authentication objects
 * without creating circular dependencies between services.
 */
public class AuthenticationValidator {

    private AuthenticationValidator() {
        // Prevent instantiation
    }

    /**
     * Validates the authentication object to ensure it is not null and contains a valid principal.
     *
     * @param authentication the authentication object to validate
     * @throws IllegalStateException if the authentication is null or does not contain a valid principal
     */
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

