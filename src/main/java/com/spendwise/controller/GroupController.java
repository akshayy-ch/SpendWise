package com.spendwise.controller;

import com.spendwise.dto.error.ErrorResponse;
import com.spendwise.dto.request.group.CreateGroupRequest;
import com.spendwise.dto.response.group.GroupResponse;
import com.spendwise.service.GroupServices;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@Tag(name = "Groups", description = "APIs for creating and managing expense-sharing groups")
public class GroupController {
    private final GroupServices groupServices;

    @Operation(summary = "Create a group")
    @PostMapping("/createGroup")
    public ResponseEntity<GroupResponse> createGroup(@Valid @RequestBody CreateGroupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(groupServices.createGroup(request));
    }

    @Operation(summary = "Get my groups")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Groups retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/my")
    public ResponseEntity<List<GroupResponse>> getMyGroups() {
        return ResponseEntity.ok(groupServices.getMyGroups());
    }

    @Operation(summary = "Archive a group")
    @PatchMapping("/{groupId}/archive")
    public ResponseEntity<GroupResponse> archiveGroup(@PathVariable UUID groupId) {
        return ResponseEntity.ok(groupServices.archiveGroup(groupId));
    }
}