package com.g13cs3219.userservice.services;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.g13cs3219.userservice.dto.responses.AuthResponse;
import com.g13cs3219.userservice.dto.responses.LoginResponse;
import com.g13cs3219.userservice.dto.requests.LoginRequest;
import com.g13cs3219.userservice.dto.requests.RegisterRequest;
import com.g13cs3219.userservice.dto.responses.LogoutResponse;
import com.g13cs3219.userservice.dto.responses.RegisterResponse;
import com.g13cs3219.userservice.model.Role;
import com.g13cs3219.userservice.model.User;
import com.g13cs3219.userservice.repositories.UserRepository;
import com.g13cs3219.userservice.utils.AuthenticationValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordService passwordService;

    public RegisterResponse register(RegisterRequest request) {
        RegisterRequest.validate(request);
        userService.validateEmail(request.getEmail());
        passwordService.validatePassword(request.getPassword());
        userService.checkEmailAlreadyExist(request.getEmail());

        String encodedPassword = passwordService.encodePassword(request.getPassword());
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .encodedPassword(encodedPassword)
                .build();

        User registeredUser = userRepository.save(user);
        return RegisterResponse.buildResponse(registeredUser.getUserId());
    }

    public LoginResponse login(LoginRequest request) {
        LoginRequest.validate(request);
        User user = userService.getUserByEmail(request.getEmail());
        passwordService.verifyPassword(user, request.getPassword());
        String token = jwtService.generateToken(request.getEmail());
        return LoginResponse.buildResponse(token, user.getUserId());
    }

    public LogoutResponse logout(Authentication authentication) {
        AuthenticationValidator.validateAuthentication(authentication);
        return LogoutResponse.buildResponse();
    }

    public AuthResponse getAuth(Authentication authentication) {
        AuthenticationValidator.validateAuthentication(authentication);
        User user = (User) authentication.getPrincipal();
        return AuthResponse.buildResponse(user.getUserId(), user.getEmail(), user.getRole());
    }
}
