package com.spendwise.service;

import com.spendwise.dto.request.user.UpdateUserRoleRequest;
import com.spendwise.dto.response.user.AdminUserResponse;
import com.spendwise.entity.User;
import com.spendwise.enums.Role;
import com.spendwise.exception.GroupMemberExceptions.UserDoesNotExist;
import com.spendwise.exception.UserExceptions.InvalidRoleChangeException;
import com.spendwise.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // No global cleanup.
        // Each test creates its own uniquely identifiable users.
    }

    @AfterEach
    void tearDown() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    private User createUser(String prefix, Role role) {

        User user = User.builder()
                .username(prefix + "_" + UUID.randomUUID())
                .name("Test User")
                .email(prefix + "_" + UUID.randomUUID() + "@test.com")
                .phoneNumber(null)
                .role(role)
                .emailVerified(true)
                .build();

        user.updatePasswordHash("test-password-hash");

        return userRepository.save(user);
    }

    @Test
    void getUser_shouldReturnUser() {

        User user = createUser("get_user", Role.USER);

        AdminUserResponse response =
                userService.getUser(user.getId());

        assertNotNull(response);
        assertEquals(user.getId(), response.getId());
        assertEquals(user.getUsername(), response.getUsername());
        assertEquals(user.getName(), response.getName());
        assertEquals(user.getEmail(), response.getEmail());
        assertEquals(Role.USER, response.getRole());
    }

    @Test
    void getUser_shouldRejectNonExistingUser() {

        UUID userId = UUID.randomUUID();

        assertThrows(
                UserDoesNotExist.class,
                () -> userService.getUser(userId)
        );
    }

    @Test
    void getUsers_shouldReturnAllUsers() {

        User user1 = createUser("list_user_1", Role.USER);
        User user2 = createUser("list_user_2", Role.USER);

        List<AdminUserResponse> responses =
                userService.getUsers();

        assertTrue(
                responses.stream()
                        .anyMatch(user -> user.getId().equals(user1.getId()))
        );

        assertTrue(
                responses.stream()
                        .anyMatch(user -> user.getId().equals(user2.getId()))
        );
    }

    @Test
    void updateUserRole_shouldPromoteUserToAdmin() {

        User user = createUser("promote_user", Role.USER);

        UpdateUserRoleRequest request =
                UpdateUserRoleRequest.builder()
                        .role(Role.ADMIN)
                        .build();

        AdminUserResponse response =
                userService.updateUserRole(user.getId(), request);

        assertNotNull(response);
        assertEquals(user.getId(), response.getId());
        assertEquals(Role.ADMIN, response.getRole());

        User updatedUser =
                userRepository.findById(user.getId()).orElseThrow();

        assertEquals(Role.ADMIN, updatedUser.getRole());
    }

    @Test
    void updateUserRole_shouldRejectAlreadyAdmin() {

        User user = createUser("already_admin", Role.ADMIN);

        UpdateUserRoleRequest request =
                UpdateUserRoleRequest.builder()
                        .role(Role.ADMIN)
                        .build();

        InvalidRoleChangeException exception =
                assertThrows(
                        InvalidRoleChangeException.class,
                        () -> userService.updateUserRole(
                                user.getId(),
                                request
                        )
                );

        assertEquals(
                "User is already an admin",
                exception.getMessage()
        );
    }

    @Test
    void updateUserRole_shouldRejectChangingToUser() {

        User user = createUser("reject_role", Role.USER);

        UpdateUserRoleRequest request =
                UpdateUserRoleRequest.builder()
                        .role(Role.USER)
                        .build();

        InvalidRoleChangeException exception =
                assertThrows(
                        InvalidRoleChangeException.class,
                        () -> userService.updateUserRole(
                                user.getId(),
                                request
                        )
                );

        assertEquals(
                "An admin role cannot be changed to user",
                exception.getMessage()
        );
    }

    @Test
    void updateUserRole_shouldRejectNonExistingUser() {

        UpdateUserRoleRequest request =
                UpdateUserRoleRequest.builder()
                        .role(Role.ADMIN)
                        .build();

        assertThrows(
                UserDoesNotExist.class,
                () -> userService.updateUserRole(
                        UUID.randomUUID(),
                        request
                )
        );
    }

    @Test
    void getUser_shouldReturnMappedAdminResponse() {

        User user = createUser("mapping_user", Role.ADMIN);

        AdminUserResponse response =
                userService.getUser(user.getId());

        assertEquals(user.getId(), response.getId());
        assertEquals(user.getUsername(), response.getUsername());
        assertEquals(user.getName(), response.getName());
        assertEquals(user.getEmail(), response.getEmail());
        assertEquals(user.getPhoneNumber(), response.getPhoneNumber());
        assertEquals(user.getRole(), response.getRole());
    }
}