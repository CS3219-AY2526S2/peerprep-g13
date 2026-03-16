package com.g13cs3219.server.services;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.g13cs3219.server.dto.requests.UpdateRoleRequest;
import com.g13cs3219.server.dto.responses.UpdateRoleResponse;
import com.g13cs3219.server.exceptions.InvalidUserIDException;
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
    private final PasswordService passwordService;

    /**
     * Promotes a user to a higher role. Only accessible by admins.
     *
     * @param targetId the ID of the user to be promoted
     * @param request the request body containing the promotion reason
     * @return a response indicating the success of the operation
     * @throws InvalidUserIDException if the user ID is invalid
     * @throws UserNotFoundException if the user with the given ID does not exist
     * @throws AccessDeniedException if the password provided does not match the user's password
     */
    @Transactional
    public UpdateRoleResponse updateRole(Long targetId, UpdateRoleRequest request) {
        // Get the admin and target user
        Long adminId = request.getAdminId();
        User admin = getUserById(adminId);
        User target = getUserById(targetId);

        // Verify admin
        verifyAdmin(admin);
        passwordService.verifyPassword(admin, request.getPassword());

        // Update the user's role
        Role role = Role.fromId(request.getNewRole());
        userRepository.updateUserRole(target, role);

        return UpdateRoleResponse.buildResponse(role);
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
     * Verifies that the given user has an admin role.
     *
     * @param admin the user to verify
     * @throws AccessDeniedException if the user does not have an admin role
     */
    public void verifyAdmin(User admin) {
        if (admin.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only admins can perform this action.");
        }
    }

    /**
     * Checks if a user with the given email exists.
     *
     * @param email the email to check for existence
     * @throws UserNotFoundException if no user with the given email exists
     */
    public void checkUserExistsByEmail(String email) {
        if (!userRepository.existsByEmail(email)) {
            throw new UserNotFoundException();
        }
    }
}
