package com.g13cs3219.server.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.g13cs3219.server.exceptions.InvalidCredentialsException;
import com.g13cs3219.server.exceptions.InvalidPasswordException;
import com.g13cs3219.server.model.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordService {

    private final PasswordEncoder passwordEncoder;

    /**
     * Validates the strength of the given password. The password must be at least 8 characters long and contain
     * at least one uppercase letter, one lowercase letter, one digit, and one special character.
     *
     * @param password the password to validate
     * @throws IllegalArgumentException if the password is null or blank
     * @throws InvalidPasswordException if the password does not meet the strength requirements
     */
    public void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or blank");
        }
        if (password.length() < 8) {
            throw new InvalidPasswordException("Password must be at least 8 characters long");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new InvalidPasswordException("Password must contain at least one uppercase letter");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new InvalidPasswordException("Password must contain at least one lowercase letter");
        }
        if (!password.matches(".*\\d.*")) {
            throw new InvalidPasswordException("Password must contain at least one digit");
        }
        if (!password.matches(".*[!@#$%^&*()].*")) {
            throw new InvalidPasswordException("Password must contain at least one special character (!@#$%^&*())");
        }
    }

    /**
     * Verifies that the raw password matches the encoded password of the user.
     *
     * @param user the user whose password is to be verified
     * @param rawPassword the raw password to be verified
     * @throws InvalidCredentialsException if the raw password does not match the user's encoded password
     */
    public void verifyPassword(User user, String rawPassword) {
        if (!passwordEncoder.matches(rawPassword, user.getEncodedPassword())) {
            throw new InvalidCredentialsException();
        }
    }

    /**
     * Encodes a raw password using the password encoder.
     *
     * @param rawPassword the raw password to be encoded
     * @return the encoded password
     */
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}
