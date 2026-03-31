package com.g13cs3219.userservice.services;

import com.g13cs3219.userservice.dto.requests.UpdateDashboardRequest;
import com.g13cs3219.userservice.dto.requests.UpdatePasswordRequest;
import com.g13cs3219.userservice.dto.responses.*;
import com.g13cs3219.userservice.repositories.HistoryRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.g13cs3219.userservice.dto.requests.UpdateRoleRequest;
import com.g13cs3219.userservice.exceptions.EmailAlreadyExistsException;
import com.g13cs3219.userservice.exceptions.InvalidEmailFormatException;
import com.g13cs3219.userservice.exceptions.UserNotFoundException;
import com.g13cs3219.userservice.model.Role;
import com.g13cs3219.userservice.model.User;
import com.g13cs3219.userservice.repositories.UserRepository;
import com.g13cs3219.userservice.utils.AuthenticationValidator;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final HistoryRepository historyRepository;
    private final PasswordService passwordService;

    @Transactional
    public UpdatePasswordResponse updatePassword(Authentication authentication, UpdatePasswordRequest request) {
        passwordService.validatePassword(request.getNewPassword());
        AuthenticationValidator.validateAuthentication(authentication);
        User user = (User) authentication.getPrincipal();
        passwordService.verifyPassword(user, request.getCurrentPassword());
        String encodedPassword = passwordService.encodePassword(request.getNewPassword());
        userRepository.updateUserPassword(user, encodedPassword);
        return UpdatePasswordResponse.buildResponse();
    }

    @Transactional
    public UpdateRoleResponse updateRole(Long targetId, UpdateRoleRequest request) {
        UpdateRoleRequest.validateRequest(request);
        User target = getUserById(targetId);
        Role role = Role.fromId(request.getNewRole());
        userRepository.updateUserRole(target, role);
        return UpdateRoleResponse.buildResponse(role);
    }

    public void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or blank.");
        }
        if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            throw new InvalidEmailFormatException();
        }
    }

    public User getUserById(Long userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(UserNotFoundException::new);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);
    }

    public void checkEmailAlreadyExist(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }
    }

    public DashboardResponse getDashboard(Long userId) {
        User user = getUserById(userId);
        List<HistoryDashboardResponse> history = historyRepository
                .findByUserIdOrderByFinishedDateDesc(userId)
                .stream()
                .map(HistoryDashboardResponse::from)
                .collect(Collectors.toList());
        return DashboardResponse.build(UserDashboardResponse.from(user), history);
    }

    @Transactional
    public UpdateDashboardResponse updateDashboard(Long userId, UpdateDashboardRequest request) {
        User user = getUserById(userId);
        if (request.getUsername() != null && !request.getUsername().isBlank())
            user.setUsername(request.getUsername());
        if (request.getName() != null)
            user.setName(request.getName());
        if (request.getAvatarUrl() != null)
            user.setAvatarUrl(request.getAvatarUrl());
        if (request.getPreferredLanguage() != null)
            user.setPreferredLanguage(request.getPreferredLanguage());
        if (request.getPreferredTopic() != null)
            user.setPreferredTopic(request.getPreferredTopic());
        userRepository.save(user);
        return UpdateDashboardResponse.success();
    }
}
