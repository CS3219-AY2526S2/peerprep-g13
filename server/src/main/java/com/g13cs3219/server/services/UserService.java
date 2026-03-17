package com.g13cs3219.server.services;

import com.g13cs3219.server.dto.requests.UpdateDashboardRequest;
import com.g13cs3219.server.dto.responses.*;
import com.g13cs3219.server.repositories.HistoryRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.g13cs3219.server.dto.requests.UpdateRoleRequest;
import com.g13cs3219.server.exceptions.EmailAlreadyExistsException;
import com.g13cs3219.server.exceptions.InvalidEmailFormatException;
import com.g13cs3219.server.exceptions.UserNotFoundException;
import com.g13cs3219.server.model.Role;
import com.g13cs3219.server.model.User;
import com.g13cs3219.server.repositories.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final HistoryRepository historyRepository;

    /**
     * Updates the role of a target user. Only admins can perform this action.
     *
     * @param targetId the ID of the user whose role is to be updated
     * @param request  the request containing the admin's ID, password, and the new role
     * @return a response containing the new role of the target user
     */
    @Transactional
    public UpdateRoleResponse updateRole(Long targetId, UpdateRoleRequest request) {
        // Validate the request
        UpdateRoleRequest.validateRequest(request);

        // Get the admin and target user
        User target = getUserById(targetId);

        // Update the user's role
        Role role = Role.fromId(request.getNewRole());
        userRepository.updateUserRole(target, role);

        return UpdateRoleResponse.buildResponse(role);
    }

    /**
     * Validates the format of the given email. The email should be in the format of 'youremail@domain'.
     *
     * @param email the email to validate
     * @throws IllegalArgumentException    if the email is null or blank
     * @throws InvalidEmailFormatException if the email does not match the required format
     */
    public void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or blank.");
        }
        if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            throw new InvalidEmailFormatException();
        }
    }

    /**
     * Retrieves a user by their ID.
     *
     * @param userId the ID of the user to retrieve
     * @return the User object corresponding to the given ID
     * @throws UserNotFoundException if no user with the given ID exists
     */
    public User getUserById(Long userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(UserNotFoundException::new);
    }

    /**
     * Retrieves a user by their email.
     *
     * @param email the email of the user to retrieve
     * @return the User object corresponding to the given email
     * @throws UserNotFoundException if no user with the given email exists
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);
    }

    /**
     * Checks if a user with the given email already exists.
     *
     * @param email the email to check for existence
     * @throws EmailAlreadyExistsException if no user with the given email exists
     */
    public void checkEmailAlreadyExist(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }
    }

    /**
     * Retrieves the dashboard data for a user, including profile and submission history.
     *
     * @param userId the ID of the target user
     * @return a DashboardResponse containing user info, history, and metrics
     */
    public DashboardResponse getDashboard(Long userId) {
        User user = getUserById(userId);

        List<HistoryDashboardResponse> history = historyRepository
                .findByUserIdOrderByFinishedDateDesc(userId)
                .stream()
                .map(HistoryDashboardResponse::from)
                .collect(Collectors.toList());

        return DashboardResponse.build(UserDashboardResponse.from(user), history);
    }

    /**
     * Updates the dashboard profile fields for a user.
     * Only non-null fields in the request are applied.
     *
     * @param userId  the ID of the target user
     * @param request the fields to update
     * @return a success message response
     */
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
