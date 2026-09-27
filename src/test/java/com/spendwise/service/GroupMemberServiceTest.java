package com.spendwise.service;

import com.spendwise.dto.request.groupMember.AddGroupMemberRequest;
import com.spendwise.dto.response.groupMember.GroupMemberResponse;
import com.spendwise.entity.Group;
import com.spendwise.entity.GroupMember;
import com.spendwise.entity.GroupMemberId;
import com.spendwise.entity.User;
import com.spendwise.enums.GroupMemberStatus;
import com.spendwise.enums.GroupStatus;
import com.spendwise.enums.Role;
import com.spendwise.exception.GroupExceptions.ArchivedGroupException;
import com.spendwise.exception.GroupExceptions.UnauthorizedGroupActionException;
import com.spendwise.exception.GroupMemberExceptions.DuplicateGroupMemberException;
import com.spendwise.exception.GroupMemberExceptions.GroupMemberDoesNotExist;
import com.spendwise.exception.GroupMemberExceptions.InactiveGroupMemberException;
import com.spendwise.exception.GroupMemberExceptions.NotGroupMemberException;
import com.spendwise.exception.GroupMemberExceptions.OutStandingBalanceException;
import com.spendwise.repository.ExpenseShareRepository;
import com.spendwise.repository.GroupMemberRepository;
import com.spendwise.repository.GroupRepository;
import com.spendwise.repository.UserRepository;
import com.spendwise.security.SpendWiseUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
class GroupMemberServiceTest {

    @Autowired
    private GroupMemberService groupMemberService;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private ExpenseShareRepository expenseShareRepository;

    @MockBean
    private NotificationService notificationService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private User creator;
    private User member;
    private User anotherUser;

    private Group group;

    @BeforeEach
    void setUp() {

        creator = createUser();
        member = createUser();
        anotherUser = createUser();

        group = createGroup(creator);

        createMembership(group, creator, GroupMemberStatus.ACTIVE);

        authenticateAs(creator);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        Mockito.reset(expenseShareRepository, notificationService);
    }

    @Test
    void addMembers_shouldAddNewMember() {

        AddGroupMemberRequest request =
                AddGroupMemberRequest.builder()
                        .userIds(List.of(member.getId()))
                        .build();

        List<GroupMemberResponse> responses =
                groupMemberService.addMembers(group.getId(), request);

        assertEquals(1, responses.size());

        GroupMemberResponse response = responses.get(0);

        assertEquals(member.getId(), response.getUserId());
        assertEquals(member.getName(), response.getUserName());
        assertEquals(GroupMemberStatus.ACTIVE, response.getStatus());

        GroupMember savedMember =
                groupMemberRepository
                        .findByGroupIdAndUserId(group.getId(), member.getId())
                        .orElseThrow();

        assertEquals(GroupMemberStatus.ACTIVE, savedMember.getStatus());
        assertEquals(member.getId(), savedMember.getUser().getId());
    }

    @Test
    void addMembers_shouldRejectDuplicateActiveMember() {

        createMembership(group, member, GroupMemberStatus.ACTIVE);

        AddGroupMemberRequest request =
                AddGroupMemberRequest.builder()
                        .userIds(List.of(member.getId()))
                        .build();

        assertThrows(
                DuplicateGroupMemberException.class,
                () -> groupMemberService.addMembers(group.getId(), request)
        );
    }

    @Test
    void addMembers_shouldReactivateLeftMember() {

        createMembership(group, member, GroupMemberStatus.LEFT);

        AddGroupMemberRequest request =
                AddGroupMemberRequest.builder()
                        .userIds(List.of(member.getId()))
                        .build();

        List<GroupMemberResponse> responses =
                groupMemberService.addMembers(group.getId(), request);

        assertEquals(1, responses.size());
        assertEquals(
                GroupMemberStatus.ACTIVE,
                responses.get(0).getStatus()
        );

        GroupMember savedMember =
                groupMemberRepository
                        .findByGroupIdAndUserId(group.getId(), member.getId())
                        .orElseThrow();

        assertEquals(
                GroupMemberStatus.ACTIVE,
                savedMember.getStatus()
        );
    }

    @Test
    void addMembers_shouldRejectNonMember() {

        Group anotherGroup = createGroup(anotherUser);
        createMembership(
                anotherGroup,
                anotherUser,
                GroupMemberStatus.ACTIVE
        );

        AddGroupMemberRequest request =
                AddGroupMemberRequest.builder()
                        .userIds(List.of(member.getId()))
                        .build();

        authenticateAs(anotherUser);

        assertThrows(
                NotGroupMemberException.class,
                () -> groupMemberService.addMembers(group.getId(), request)
        );
    }

    @Test
    void addMembers_shouldRejectInactiveCurrentMember() {

        GroupMember creatorMembership =
                groupMemberRepository
                        .findByGroupIdAndUserId(
                                group.getId(),
                                creator.getId()
                        )
                        .orElseThrow();

        creatorMembership.setStatus(GroupMemberStatus.LEFT);
        groupMemberRepository.save(creatorMembership);

        AddGroupMemberRequest request =
                AddGroupMemberRequest.builder()
                        .userIds(List.of(member.getId()))
                        .build();

        assertThrows(
                InactiveGroupMemberException.class,
                () -> groupMemberService.addMembers(group.getId(), request)
        );
    }

    @Test
    void addMembers_shouldRejectArchivedGroup() {

        jdbcTemplate.update(
                """
                UPDATE groups
                SET status = 'ARCHIVED',
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """,
                group.getId()
        );

        AddGroupMemberRequest request =
                AddGroupMemberRequest.builder()
                        .userIds(List.of(member.getId()))
                        .build();

        assertThrows(
                ArchivedGroupException.class,
                () -> groupMemberService.addMembers(group.getId(), request)
        );
    }

    @Test
    void leaveGroup_shouldMarkMemberAsLeft() {

        createMembership(group, member, GroupMemberStatus.ACTIVE);

        authenticateAs(member);

        when(
                expenseShareRepository.hasOutstandingBalance(
                        eq(group.getId()),
                        eq(member.getId())
                )
        ).thenReturn(false);

        GroupMemberResponse response =
                groupMemberService.leaveGroup(group.getId());

        assertEquals(member.getId(), response.getUserId());
        assertEquals(GroupMemberStatus.LEFT, response.getStatus());

        GroupMember savedMember =
                groupMemberRepository
                        .findByGroupIdAndUserId(group.getId(), member.getId())
                        .orElseThrow();

        assertEquals(
                GroupMemberStatus.LEFT,
                savedMember.getStatus()
        );
    }

    @Test
    void leaveGroup_shouldRejectNonMember() {

        authenticateAs(anotherUser);

        assertThrows(
                NotGroupMemberException.class,
                () -> groupMemberService.leaveGroup(group.getId())
        );
    }

    @Test
    void leaveGroup_shouldRejectInactiveMember() {

        createMembership(group, member, GroupMemberStatus.LEFT);

        authenticateAs(member);

        assertThrows(
                InactiveGroupMemberException.class,
                () -> groupMemberService.leaveGroup(group.getId())
        );
    }

    @Test
    void leaveGroup_shouldRejectOutstandingBalance() {

        createMembership(group, member, GroupMemberStatus.ACTIVE);

        authenticateAs(member);

        when(
                expenseShareRepository.hasOutstandingBalance(
                        eq(group.getId()),
                        eq(member.getId())
                )
        ).thenReturn(true);

        assertThrows(
                OutStandingBalanceException.class,
                () -> groupMemberService.leaveGroup(group.getId())
        );

        GroupMember savedMember =
                groupMemberRepository
                        .findByGroupIdAndUserId(group.getId(), member.getId())
                        .orElseThrow();

        assertEquals(
                GroupMemberStatus.ACTIVE,
                savedMember.getStatus()
        );
    }

    @Test
    void removeMember_shouldRemoveActiveMember() {

        createMembership(group, member, GroupMemberStatus.ACTIVE);

        when(
                expenseShareRepository.hasOutstandingBalance(
                        eq(group.getId()),
                        eq(member.getId())
                )
        ).thenReturn(false);

        GroupMemberResponse response =
                groupMemberService.removeMember(
                        group.getId(),
                        member.getId()
                );

        assertEquals(member.getId(), response.getUserId());
        assertEquals(GroupMemberStatus.LEFT, response.getStatus());

        GroupMember savedMember =
                groupMemberRepository
                        .findByGroupIdAndUserId(group.getId(), member.getId())
                        .orElseThrow();

        assertEquals(
                GroupMemberStatus.LEFT,
                savedMember.getStatus()
        );
    }

    @Test
    void removeMember_shouldRejectNonCreator() {

        createMembership(group, member, GroupMemberStatus.ACTIVE);

        authenticateAs(member);

        assertThrows(
                UnauthorizedGroupActionException.class,
                () -> groupMemberService.removeMember(
                        group.getId(),
                        anotherUser.getId()
                )
        );
    }

    @Test
    void removeMember_shouldRejectMissingMember() {

        assertThrows(
                GroupMemberDoesNotExist.class,
                () -> groupMemberService.removeMember(
                        group.getId(),
                        anotherUser.getId()
                )
        );
    }

    @Test
    void removeMember_shouldRejectInactiveMember() {

        createMembership(group, member, GroupMemberStatus.LEFT);

        assertThrows(
                InactiveGroupMemberException.class,
                () -> groupMemberService.removeMember(
                        group.getId(),
                        member.getId()
                )
        );
    }

    @Test
    void removeMember_shouldRejectOutstandingBalance() {

        createMembership(group, member, GroupMemberStatus.ACTIVE);

        when(
                expenseShareRepository.hasOutstandingBalance(
                        eq(group.getId()),
                        eq(member.getId())
                )
        ).thenReturn(true);

        assertThrows(
                OutStandingBalanceException.class,
                () -> groupMemberService.removeMember(
                        group.getId(),
                        member.getId()
                )
        );

        GroupMember savedMember =
                groupMemberRepository
                        .findByGroupIdAndUserId(group.getId(), member.getId())
                        .orElseThrow();

        assertEquals(
                GroupMemberStatus.ACTIVE,
                savedMember.getStatus()
        );
    }

    private User createUser() {

        String unique = UUID.randomUUID().toString();

        User user = User.builder()
                .username("group_member_test_" + unique)
                .name("Group Member Test User")
                .email("group_member_test_" + unique + "@example.com")
                .phoneNumber(
                        "9" + unique.replaceAll("[^0-9]", "").substring(0, 9)
                )
                .role(Role.USER)
                .emailVerified(true)
                .build();

        user.updatePasswordHash("password");

        return userRepository.save(user);
    }

    private Group createGroup(User creator) {

        Group group = Group.builder()
                .name("Group_" + UUID.randomUUID())
                .description("Group member test group")
                .status(GroupStatus.ACTIVE)
                .user(creator)
                .build();

        return groupRepository.save(group);
    }

    private GroupMember createMembership(
            Group group,
            User user,
            GroupMemberStatus status
    ) {

        GroupMemberId id =
                new GroupMemberId(
                        group.getId(),
                        user.getId()
                );

        GroupMember member =
                GroupMember.builder()
                        .id(id)
                        .group(group)
                        .user(user)
                        .status(status)
                        .build();

        return groupMemberRepository.save(member);
    }

    private void authenticateAs(User user) {

        SpendWiseUserDetails userDetails =
                new SpendWiseUserDetails(user);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }
}
