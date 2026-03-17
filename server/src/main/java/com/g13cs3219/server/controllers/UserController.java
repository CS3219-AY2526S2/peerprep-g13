package com.g13cs3219.server.controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.g13cs3219.server.dto.requests.UpdateRoleRequest;
import com.g13cs3219.server.dto.responses.UpdateRoleResponse;
import com.g13cs3219.server.services.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PatchMapping(path = "/user/{targetId}/role")
    public ResponseEntity<Map<String, Object>> updateRole(@PathVariable("targetId") Long targetId,
                                                          Authentication authentication,
                                                          @RequestBody UpdateRoleRequest promoteRequest) {
        UpdateRoleResponse updateRoleResponse = userService.updateRole(targetId, authentication, promoteRequest);
        return ResponseEntity.ok(Map.of("data", updateRoleResponse));
    }
}
