package com.g13cs3219.server.services;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.g13cs3219.server.dto.requests.UpdateRoleRequest;
import com.g13cs3219.server.dto.responses.UpdateRoleResponse;
import com.g13cs3219.server.exceptions.EmailAlreadyExistsException;
import com.g13cs3219.server.exceptions.InvalidEmailFormatException;
import com.g13cs3219.server.exceptions.UserNotFoundException;
import com.g13cs3219.server.model.Role;
import com.g13cs3219.server.model.User;
import com.g13cs3219.server.repositories.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;

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
}
