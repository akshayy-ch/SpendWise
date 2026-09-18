package com.spendwise.controller;

import com.spendwise.dto.request.user.UpdateUserRoleRequest;
import com.spendwise.dto.response.user.AdminUserResponse;
import com.spendwise.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{userId}")
    public ResponseEntity<AdminUserResponse> getUser(
            @PathVariable UUID userId) {

        AdminUserResponse response = userService.getUser(userId);

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{userId}/role")
    public ResponseEntity<AdminUserResponse> updateUserRole(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserRoleRequest request) {

        AdminUserResponse response =
                userService.updateUserRole(userId, request);

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<AdminUserResponse>> getUsers() {

        List<AdminUserResponse> response = userService.getUsers();

        return ResponseEntity.ok(response);
    }
}