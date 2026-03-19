package com.g13cs3219.server.services;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.g13cs3219.server.dto.responses.AuthResponse;
import com.g13cs3219.server.dto.responses.LoginResponse;
import com.g13cs3219.server.dto.requests.LoginRequest;
import com.g13cs3219.server.dto.requests.RegisterRequest;
import com.g13cs3219.server.dto.responses.LogoutResponse;
import com.g13cs3219.server.dto.responses.RegisterResponse;
import com.g13cs3219.server.model.Role;
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
        // Validate input
        RegisterRequest.validate(request);
        userService.validateEmail(request.getEmail());
        passwordService.validatePassword(request.getPassword());

        // Encode the password before saving
        String encodedPassword = passwordService.encodePassword(request.getPassword());

        // Check if email already exists
        userService.checkEmailAlreadyExist(request.getEmail());

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

    /**
     * Logs out the currently authenticated user. This method can be used to perform any necessary cleanup
     * related to the user's session or authentication state.
     *
     * @param authentication the authentication object containing the user's details
     * @return a response indicating that the logout was successful
     */
    public LogoutResponse logout(Authentication authentication) {
        // Validate the authentication object
        validateAuthentication(authentication);
        User user = (User) authentication.getPrincipal();

        return LogoutResponse.buildResponse();
    }

    /**
     * Retrieves the authentication information of the currently authenticated user.
     *
     * @param authentication the authentication object containing the user's details
     * @return a response containing the user's ID, email, and role
     */
    public AuthResponse getAuth(Authentication authentication) {
        // Validate the authentication object
        validateAuthentication(authentication);
        User user = (User) authentication.getPrincipal();

        // Extract user details from the authentication objects
        Long userId = user.getUserId();
        String email = user.getEmail();
        Role role = user.getRole();

        return AuthResponse.buildResponse(userId, email, role);
    }

    /**
     * Validates the authentication object to ensure it is not null and contains a valid principal.
     *
     * @param authentication the authentication object to validate
     * @throws IllegalStateException if the authentication is null or does not contain a valid principal
     */
    public void validateAuthentication(Authentication authentication) {
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
