package com.spendwise.service;

import com.spendwise.dto.request.groupMember.AddGroupMemberRequest;
import com.spendwise.dto.response.groupMember.GroupMemberResponse;
import com.spendwise.entity.Group;
import com.spendwise.entity.GroupMember;
import com.spendwise.entity.GroupMemberId;
import com.spendwise.entity.User;
import com.spendwise.enums.GroupMemberStatus;
import com.spendwise.enums.GroupStatus;
import com.spendwise.exception.GroupExceptions.ArchivedGroupException;
import com.spendwise.exception.GroupExceptions.GroupDoesNotExist;
import com.spendwise.exception.GroupExceptions.UnauthorizedGroupActionException;
import com.spendwise.exception.GroupMemberExceptions.*;
import com.spendwise.repository.ExpenseShareRepository;
import com.spendwise.repository.GroupMemberRepository;
import com.spendwise.repository.GroupRepository;
import com.spendwise.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GroupMemberService {

    private final CurrentUserService currentUserService;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final ExpenseShareRepository expenseShareRepository;

    @Transactional
    public List<GroupMemberResponse> addMembers(UUID groupId, AddGroupMemberRequest request) {

        UUID currentUserId = currentUserService.getCurrentUserId();

        Group group = groupRepository.findById(groupId).orElseThrow(() -> new GroupDoesNotExist("Group not found"));

        GroupMemberId currentMemberId = new GroupMemberId(groupId, currentUserId);

        GroupMember currentMember = groupMemberRepository.findById(currentMemberId) .orElseThrow(() -> new NotGroupMemberException("You are not a member of this group"));

        if (currentMember.getStatus() != GroupMemberStatus.ACTIVE) {
            throw new InactiveGroupMemberException("Only active members can add users");
        }

        if (group.getStatus() != GroupStatus.ACTIVE) {throw new ArchivedGroupException("Cannot add members to an archived group");
        }

        List<GroupMemberResponse> responses = new ArrayList<>();

        for (UUID userId : request.getUserIds()) {
            User user = userRepository.findById(userId).orElseThrow(() -> new UserDoesNotExist("User not found: " + userId));

            GroupMemberId memberId = new GroupMemberId(groupId, userId);


            GroupMember member = groupMemberRepository .findById(memberId) .orElse(null);

            if (member == null) {
                member = GroupMember.builder()
                        .id(memberId)
                        .group(group)
                        .user(user)
                        .status(GroupMemberStatus.ACTIVE)
                        .build();
            } else {
                if (member.getStatus() == GroupMemberStatus.ACTIVE) {
                    throw new DuplicateGroupMemberException("User is already an active member");
                }
                member.setStatus(GroupMemberStatus.ACTIVE);
            }

            GroupMember savedMember = groupMemberRepository.save(member);

            responses.add(
                    GroupMemberResponse.builder()
                            .userId(
                                    savedMember.getUser().getId()
                            )
                            .userName(
                                    savedMember.getUser().getName()
                            )
                            .status(
                                    savedMember.getStatus()
                            )
                            .joinedAt(
                                    savedMember.getJoinedAt()
                            )
                            .build()
            );
        }

        return responses;
    }

    @Transactional
    public GroupMemberResponse leaveGroup(UUID groupId) {

        UUID currentUserId = currentUserService.getCurrentUserId();

        GroupMemberId memberId = new GroupMemberId( groupId,  currentUserId );

        GroupMember member = groupMemberRepository.findById(memberId).orElseThrow(() ->new NotGroupMemberException("You are not a member of this group"));

        if (member.getStatus() != GroupMemberStatus.ACTIVE) {
            throw new InactiveGroupMemberException("You are not an active member of this group");
        }

        boolean hasOutstandingBalance =
                expenseShareRepository.hasOutstandingBalance(
                        groupId,
                        currentUserId
                );
        if (hasOutstandingBalance) {
            throw new OutStandingBalanceException("You cannot leave the group while you have an outstanding balance");
        }

        member.setStatus(GroupMemberStatus.LEFT);

        GroupMember savedMember = groupMemberRepository.save(member);

        return GroupMemberResponse.builder()
                .userId(
                        savedMember.getUser().getId()
                )
                .userName(
                        savedMember.getUser().getName()
                )
                .status(
                        savedMember.getStatus()
                )
                .joinedAt(
                        savedMember.getJoinedAt()
                )
                .build();
    }

    @Transactional
    public GroupMemberResponse removeMember(UUID groupId, UUID userId) {

        UUID currentUserId = currentUserService.getCurrentUserId();

        Group group = groupRepository.findById(groupId).orElseThrow(() -> new GroupDoesNotExist("Group not found"));

        if (!group.getUser().getId().equals(currentUserId)) {
            throw new UnauthorizedGroupActionException("Only the group creator can remove members");
        }

        GroupMemberId memberId = new GroupMemberId(groupId, userId);

        GroupMember member = groupMemberRepository.findById(memberId).orElseThrow(() -> new GroupMemberDoesNotExist("Member not found"));

        if (member.getStatus() != GroupMemberStatus.ACTIVE) {
            throw new InactiveGroupMemberException("User is not an active member of this group");
        }

        boolean hasOutstandingBalance =
                expenseShareRepository.hasOutstandingBalance(
                        groupId,
                        userId
                );
        if (hasOutstandingBalance) {
            throw new OutStandingBalanceException("Cannot remove a member who has an outstanding balance");
        }

        member.setStatus(GroupMemberStatus.LEFT);

        GroupMember savedMember = groupMemberRepository.save(member);

        return GroupMemberResponse.builder()
                .userId(
                        savedMember.getUser().getId()
                )
                .userName(
                           savedMember.getUser().getName()
                )
                .status(
                        savedMember.getStatus()
                )
                .joinedAt(
                        savedMember.getJoinedAt()
                )
                .build();
    }
}