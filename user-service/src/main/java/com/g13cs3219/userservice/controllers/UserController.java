package com.g13cs3219.userservice.controllers;

import java.util.Map;

import com.g13cs3219.userservice.dto.requests.UpdateDashboardRequest;
import com.g13cs3219.userservice.dto.requests.UpdatePasswordRequest;
import com.g13cs3219.userservice.dto.responses.DashboardResponse;
import com.g13cs3219.userservice.dto.responses.UpdateDashboardResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.g13cs3219.userservice.dto.requests.UpdateRoleRequest;
import com.g13cs3219.userservice.dto.responses.UpdatePasswordResponse;
import com.g13cs3219.userservice.dto.responses.UpdateRoleResponse;
import com.g13cs3219.userservice.services.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PatchMapping("/user/password")
    public ResponseEntity<Map<String, Object>> updatePassword(Authentication authentication,
                                                              @RequestBody UpdatePasswordRequest request) {
        UpdatePasswordResponse response = userService.updatePassword(authentication, request);
        return ResponseEntity.ok(Map.of("data", response));
    }

    @PatchMapping("/user/{targetId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateRole(@PathVariable("targetId") Long targetId,
                                                          @RequestBody UpdateRoleRequest request) {
        UpdateRoleResponse response = userService.updateRole(targetId, request);
        return ResponseEntity.ok(Map.of("data", response));
    }

    @GetMapping("/user/dashboard/{userId}")
    @PreAuthorize("authentication.principal.getUserId() == #userId or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getDashboard(@PathVariable Long userId) {
        DashboardResponse dashboard = userService.getDashboard(userId);
        return ResponseEntity.ok(Map.of("data", dashboard));
    }

    @PatchMapping("/user/dashboard/{userId}")
    @PreAuthorize("authentication.principal.getUserId() == #userId or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateDashboard(@PathVariable Long userId,
                                                               @RequestBody UpdateDashboardRequest request) {
        UpdateDashboardResponse response = userService.updateDashboard(userId, request);
        return ResponseEntity.ok(Map.of("data", response));
    }
}
