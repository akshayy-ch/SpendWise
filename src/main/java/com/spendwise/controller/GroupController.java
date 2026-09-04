package com.spendwise.controller;

import com.spendwise.dto.request.group.CreateGroupRequest;
import com.spendwise.dto.response.group.GroupResponse;
import com.spendwise.service.GroupServices;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {
    private final GroupServices groupServices;

    @PostMapping("/createGroup")
    public ResponseEntity<GroupResponse> createGroup(@Valid @RequestBody CreateGroupRequest request){
        GroupResponse response = groupServices.createGroup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @PatchMapping("/{groupId}/archive")
    public ResponseEntity<GroupResponse> archiveGroup(@PathVariable UUID groupId) {
        GroupResponse response = groupServices.archiveGroup(groupId);
        return ResponseEntity.ok(response);
    }
}