package com.spendwise.controller;

import com.spendwise.dto.error.ErrorResponse;
import com.spendwise.dto.request.groupMember.AddGroupMemberRequest;
import com.spendwise.dto.response.groupMember.GroupMemberResponse;
import com.spendwise.service.GroupMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/groups/{groupId}/members")
@RequiredArgsConstructor
@Tag(name = "Group Members", description = "APIs for adding, removing, and managing members of expense-sharing groups")
public class GroupMemberController {
    private final GroupMemberService groupMemberService;

    @Operation(summary = "Get group members")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Members retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "User is not a member")
    })
    @GetMapping
    public ResponseEntity<List<GroupMemberResponse>> getMembers(@PathVariable UUID groupId) {
        return ResponseEntity.ok(groupMemberService.getMembers(groupId));
    }

    @Operation(summary = "Add group members")
    @PostMapping
    public ResponseEntity<List<GroupMemberResponse>> addMembers(
            @PathVariable UUID groupId,
            @Valid @RequestBody AddGroupMemberRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(groupMemberService.addMembers(groupId, request));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<GroupMemberResponse> removeMember(@PathVariable UUID groupId, @PathVariable UUID userId) {
        return ResponseEntity.ok(groupMemberService.removeMember(groupId, userId));
    }

    @PostMapping("/leave")
    public ResponseEntity<GroupMemberResponse> leaveGroup(@PathVariable UUID groupId) {
        return ResponseEntity.ok(groupMemberService.leaveGroup(groupId));
    }
}