package com.spendwise.controller;

import com.spendwise.dto.request.groupMember.AddGroupMemberRequest;
import com.spendwise.dto.response.groupMember.GroupMemberResponse;
import com.spendwise.service.GroupMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/groups/{groupId}/members")
@RequiredArgsConstructor
public class GroupMemberController {
    private final GroupMemberService groupMemberService;

    @PostMapping
    public ResponseEntity<List<GroupMemberResponse>> addMembers(@PathVariable UUID groupId, @Valid @RequestBody AddGroupMemberRequest request) {
        List<GroupMemberResponse> response = groupMemberService.addMembers(groupId, request);
        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/{userId}")
    public ResponseEntity<GroupMemberResponse> removeMember(@PathVariable UUID groupId, @PathVariable UUID userId) {
        GroupMemberResponse response = groupMemberService.removeMember(groupId, userId);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/leave")
    public ResponseEntity<GroupMemberResponse> leaveGroup(@PathVariable UUID groupId) {
        GroupMemberResponse response = groupMemberService.leaveGroup(groupId);
        return ResponseEntity.ok(response);
    }
}