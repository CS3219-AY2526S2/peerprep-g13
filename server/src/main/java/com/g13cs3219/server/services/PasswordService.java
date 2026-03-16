package com.g13cs3219.server.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.g13cs3219.server.exceptions.InvalidCredentialsException;
import com.g13cs3219.server.model.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordService {

    private final PasswordEncoder passwordEncoder;

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
