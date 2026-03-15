package com.g13cs3219.server.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.g13cs3219.server.dto.AuthResponse;
import com.g13cs3219.server.dto.LoginRequest;
import com.g13cs3219.server.dto.User;
import com.g13cs3219.server.exceptions.EmailAlreadyExistsException;
import com.g13cs3219.server.exceptions.EmptyArgumentException;
import com.g13cs3219.server.exceptions.InvalidCredentialsException;
import com.g13cs3219.server.exceptions.UserNotFoundException;
import com.g13cs3219.server.mapper.Mapper;
import com.g13cs3219.server.model.UserEntity;
import com.g13cs3219.server.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final Mapper<UserEntity, User> userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * Registers a new user. Returns the ID of the newly registered user.
     *
     * @param user the register data from the user
     * @return the ID of the newly registered user
     * @throws EmptyArgumentException 400 if either the email or the password is missing
     * @throws EmailAlreadyExistsException 400 if there is an account with the same email in the database
     */
    public Long register(User user) {
        // Validate input
        if (user.getEmail() == null || user.getPassword() == null) {
            throw new EmptyArgumentException();
        }

        // Encode the password before saving
        user.setPassword(encodePassword(user.getPassword()));

        // Check if email already exists
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new EmailAlreadyExistsException(user.getEmail());
        }

        // Map given dto to entity
        UserEntity userEntity = userMapper.toEntity(user);

        UserEntity savedUserEntity = userRepository.save(userEntity);

        // Map saved entity to dto
        User registeredUser = userMapper.toDto(savedUserEntity);
        return registeredUser.getUserId();
    }

    /**
     * Authenticates a user and returns an AuthResponse containing a JWT token if successful.
     *
     * @param request the login data from the user
     * @return an AuthResponse containing a JWT token if the login is successful
     * @throws EmptyArgumentException 400 if either the email or the password is missing
     * @throws UserNotFoundException 404 if not exist an account with the given email
     * @throws InvalidCredentialsException 401 if either the email or the password given is not correct
     */
    public AuthResponse login(LoginRequest request) {
        // Validate input
        if (request.getEmail() == null || request.getPassword() == null) {
            throw new EmptyArgumentException();
        }

        UserEntity userEntity = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException(request.getEmail()));

        // Check if password matches
        if (!passwordEncoder.matches(request.getPassword(), userEntity.getEncodedPassword())) {
            throw new InvalidCredentialsException();
        }

        // Generate JWT token
        String token = jwtService.generateToken(request.getEmail());
        return AuthResponse.builder()
                .message("Login Successful")
                .accessToken(token)
                .userId(userEntity.getUserId())
                .build();
    }

    /**
     * Encodes the raw password using the configured PasswordEncoder.
     */
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}
