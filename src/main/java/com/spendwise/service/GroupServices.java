package com.spendwise.service;

import com.spendwise.dto.request.group.CreateGroupRequest;
import com.spendwise.dto.response.group.GroupResponse;
import com.spendwise.entity.Group;
import com.spendwise.entity.GroupMember;
import com.spendwise.entity.GroupMemberId;
import com.spendwise.entity.User;
import com.spendwise.enums.GroupMemberStatus;
import com.spendwise.enums.GroupStatus;
import com.spendwise.exception.GroupExceptions.ArchivedGroupException;
import com.spendwise.exception.GroupExceptions.GroupDoesNotExist;
import com.spendwise.exception.GroupExceptions.UnauthorizedGroupActionException;
import com.spendwise.exception.GroupMemberExceptions.OutStandingBalanceException;
import com.spendwise.mapper.GroupMapper;
import com.spendwise.repository.ExpenseShareRepository;
import com.spendwise.repository.GroupMemberRepository;
import com.spendwise.repository.GroupRepository;
import com.spendwise.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GroupServices {
    private final CurrentUserService currentUserService;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final ExpenseShareRepository expenseShareRepository;
    private final GroupMapper groupMapper;
    private final UserRepository userRepository;


    @Transactional
    public GroupResponse createGroup(CreateGroupRequest request){
        UUID userId = currentUserService.getCurrentUserId();
        User user = userRepository.getReferenceById(userId);

        Group group = Group.builder()
                .name(request.getName())
                .description(request.getDescription())
                .status(GroupStatus.ACTIVE)
                .user(user)
                .build();

        Group createdGroup = groupRepository.save(group);

        GroupMemberId groupMemberId =
                new GroupMemberId(
                        createdGroup.getId(),
                        user.getId()
                );

        GroupMember groupMember = GroupMember.builder()
                .id(groupMemberId)
                .group(createdGroup)
                .user(user)
                .status(GroupMemberStatus.ACTIVE)
                .build();

        groupMemberRepository.save(groupMember);
        return GroupResponse.builder()
                .name(createdGroup.getName())
                .status(String.valueOf(createdGroup.getStatus()))
                .creatorName(user.getName())
                .build();
    }

    @Transactional
    public GroupResponse archiveGroup(UUID groupId) {

        UUID currentUserId = currentUserService.getCurrentUserId();

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupDoesNotExist("Group not found"));

        if (!group.getUser().getId().equals(currentUserId)) {
            throw new UnauthorizedGroupActionException("Only the group creator can archive the group");
        }

        if (group.getStatus() == GroupStatus.ARCHIVED) {
            throw new ArchivedGroupException("Group is already archived");
        }

        boolean hasOutstandingBalance =
                expenseShareRepository.hasOutstandingShares(groupId);
        if (hasOutstandingBalance) {
            throw new OutStandingBalanceException(
                    "Group cannot be archived while outstanding balances exist"
            );
        }

        group.setStatus(GroupStatus.ARCHIVED);

        Group archivedGroup = groupRepository.save(group);

        return groupMapper.toResponse(archivedGroup);
    }
}
