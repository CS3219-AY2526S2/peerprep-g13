package com.g13cs3219.server.controllers;

import java.util.Map;

import com.g13cs3219.server.dto.requests.UpdateDashboardRequest;
import com.g13cs3219.server.dto.responses.DashboardResponse;
import com.g13cs3219.server.dto.responses.UpdateDashboardResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.g13cs3219.server.dto.requests.UpdateRoleRequest;
import com.g13cs3219.server.dto.responses.UpdateRoleResponse;
import com.g13cs3219.server.services.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PutMapping(path = "/user/{targetId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateRole(@PathVariable("targetId") Long targetId,
                                                          @RequestBody UpdateRoleRequest promoteRequest) {
        UpdateRoleResponse updateRoleResponse = userService.updateRole(targetId, promoteRequest);
        return ResponseEntity.ok(Map.of("data", updateRoleResponse));
    }
    @GetMapping(path = "/user/dashboard/{userId}")
    public ResponseEntity<Map<String, Object>> getDashboard(@PathVariable Long userId) {
        DashboardResponse dashboard = userService.getDashboard(userId);
        return ResponseEntity.ok(Map.of("data", dashboard));
    }

    @PatchMapping(path = "/user/dashboard/{userId}")
    public ResponseEntity<Map<String, Object>> updateDashboard(
            @PathVariable Long userId,
            @RequestBody UpdateDashboardRequest request) {
        UpdateDashboardResponse response = userService.updateDashboard(userId, request);
        return ResponseEntity.ok(Map.of("data", response));
    }
}
