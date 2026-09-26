package com.spendwise.controller;

import com.spendwise.dto.error.ErrorResponse;
import com.spendwise.dto.request.groupMember.AddGroupMemberRequest;
import com.spendwise.dto.response.groupMember.GroupMemberResponse;
import com.spendwise.service.GroupMemberService;
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
@RequestMapping("/api/groups/{groupId}/members")
@RequiredArgsConstructor
@Tag(
        name = "Group Members",
        description = "APIs for adding, removing, and managing members of expense-sharing groups"
)
public class GroupMemberController {

    private final GroupMemberService groupMemberService;


    @Operation(
            summary = "Add group members",
            description = "Adds one or more users to an existing expense-sharing group."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Group members added successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid member data",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User is not authorized to manage this group",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Group or user not found",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "User is already a member or request conflicts with existing data",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PostMapping
    public ResponseEntity<List<GroupMemberResponse>> addMembers(
            @PathVariable UUID groupId,
            @Valid @RequestBody AddGroupMemberRequest request) {

        List<GroupMemberResponse> response =
                groupMemberService.addMembers(groupId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @Operation(
            summary = "Remove a group member",
            description = "Removes a user from an expense-sharing group."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Group member removed successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User is not authorized to manage this group",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Group or member not found",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Member cannot be removed in the current state",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @DeleteMapping("/{userId}")
    public ResponseEntity<GroupMemberResponse> removeMember(
            @PathVariable UUID groupId,
            @PathVariable UUID userId) {

        GroupMemberResponse response =
                groupMemberService.removeMember(groupId, userId);

        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "Leave a group",
            description = "Allows the authenticated user to leave an expense-sharing group."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully left the group"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Group or membership not found",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "User cannot leave the group in the current state",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PostMapping("/leave")
    public ResponseEntity<GroupMemberResponse> leaveGroup(
            @PathVariable UUID groupId) {

        GroupMemberResponse response =
                groupMemberService.leaveGroup(groupId);

        return ResponseEntity.ok(response);
    }
}