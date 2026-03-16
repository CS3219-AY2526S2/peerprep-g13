package com.g13cs3219.server.services;

import org.springframework.stereotype.Service;

import com.g13cs3219.server.dto.responses.LoginResponse;
import com.g13cs3219.server.dto.requests.LoginRequest;
import com.g13cs3219.server.dto.requests.RegisterRequest;
import com.g13cs3219.server.dto.responses.RegisterResponse;
import com.g13cs3219.server.model.User;
import com.g13cs3219.server.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordService passwordService;

    /**
     * Registers a new user. Returns the ID of the newly registered user.
     *
     * @param request the register data from the user
     * @return the ID of the newly registered user
     */
    public RegisterResponse register(RegisterRequest request) {
        // Validate the request body
        RegisterRequest.validate(request);

        // Encode the password before saving
        String encodedPassword = passwordService.encodePassword(request.getPassword());

        // Check if email already exists
        userService.checkUserExistsByEmail(request.getEmail());

        // Build a new user with input fields
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .encodedPassword(encodedPassword)
                .build();

        // Save the new user to the database
        User registeredUser = userRepository.save(user);

        return RegisterResponse.buildResponse(registeredUser.getUserId());
    }

    /**
     * Authenticates a user and returns a response containing a JWT token if successful.
     *
     * @param request the login data from the user
     * @return a response containing a JWT token if authentication is successful
     */
    public LoginResponse login(LoginRequest request) {
        // Validate the request body
        LoginRequest.validate(request);

        // Get the user by email
        User user = userService.getUserByEmail(request.getEmail());

        // Verify the password
        passwordService.verifyPassword(user, request.getPassword());

        // Generate JWT token
        String token = jwtService.generateToken(request.getEmail());

        return LoginResponse.buildResponse(token, user.getUserId());
    }
}
