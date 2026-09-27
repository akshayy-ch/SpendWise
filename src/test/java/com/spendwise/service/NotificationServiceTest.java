package com.spendwise.service;

import com.spendwise.dto.request.notification.GetNotificationRequest;
import com.spendwise.dto.response.notification.NotificationPageResponse;
import com.spendwise.dto.response.notification.NotificationResponse;
import com.spendwise.entity.Notification;
import com.spendwise.entity.User;
import com.spendwise.enums.NotificationType;
import com.spendwise.enums.Role;
import com.spendwise.exception.NotificationExceptions.NotificationDoesNotExistException;
import com.spendwise.exception.PaginationException.InvalidPaginationException;
import com.spendwise.repository.NotificationRepository;
import com.spendwise.repository.UserRepository;
import com.spendwise.security.SpendWiseUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class NotificationServiceTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;
    private User otherUser;

    @BeforeEach
    void setUp() {

        user = createUser("notification_user");
        otherUser = createUser("notification_other");

        setAuthenticatedUser(user);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createNotification_shouldCreateNotification() {

        UUID referenceId = UUID.randomUUID();

        NotificationResponse response =
                notificationService.createNotification(
                        user,
                        NotificationType.EXPENSE_SHARE_CREATED,
                        "New expense share",
                        "You have a new expense share",
                        referenceId
                );

        assertNotNull(response.getId());
        assertEquals(NotificationType.EXPENSE_SHARE_CREATED, response.getType());
        assertEquals("New expense share", response.getTitle());
        assertEquals("You have a new expense share", response.getMessage());
        assertEquals(referenceId, response.getReferenceId());
        assertFalse(response.isRead());

        Notification saved =
                notificationRepository.findById(response.getId()).orElseThrow();

        assertEquals(user.getId(), saved.getRecipient().getId());
        assertFalse(saved.isRead());
    }

    @Test
    void getMyNotifications_shouldReturnOnlyCurrentUserNotifications() {

        notificationService.createNotification(
                user,
                NotificationType.BUDGET_EXCEEDED,
                "Budget",
                "Budget exceeded",
                UUID.randomUUID()
        );

        notificationService.createNotification(
                otherUser,
                NotificationType.SETTLEMENT_RECEIVED,
                "Settlement",
                "Settlement received",
                UUID.randomUUID()
        );

        NotificationPageResponse response =
                notificationService.getMyNotifications(
                        GetNotificationRequest.builder().build(),
                        PageRequest.of(0, 10)
                );

        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getContent().size());
        assertEquals("Budget", response.getContent().get(0).getTitle());
    }

    @Test
    void getMyNotifications_shouldFilterUnreadNotifications() {

        NotificationResponse unread =
                notificationService.createNotification(
                        user,
                        NotificationType.BUDGET_EXCEEDED,
                        "Unread",
                        "Unread notification",
                        UUID.randomUUID()
                );

        NotificationResponse read =
                notificationService.createNotification(
                        user,
                        NotificationType.SETTLEMENT_RECEIVED,
                        "Read",
                        "Read notification",
                        UUID.randomUUID()
                );

        notificationService.markAsRead(read.getId());

        NotificationPageResponse response =
                notificationService.getMyNotifications(
                        GetNotificationRequest.builder()
                                .unread(true)
                                .build(),
                        PageRequest.of(0, 10)
                );

        assertEquals(1, response.getTotalElements());
        assertEquals("Unread", response.getContent().get(0).getTitle());
    }

    @Test
    void getMyNotifications_shouldFilterReadNotifications() {

        NotificationResponse read =
                notificationService.createNotification(
                        user,
                        NotificationType.BUDGET_EXCEEDED,
                        "Read",
                        "Read notification",
                        UUID.randomUUID()
                );

        notificationService.createNotification(
                user,
                NotificationType.SETTLEMENT_RECEIVED,
                "Unread",
                "Unread notification",
                UUID.randomUUID()
        );

        notificationService.markAsRead(read.getId());

        NotificationPageResponse response =
                notificationService.getMyNotifications(
                        GetNotificationRequest.builder()
                                .unread(false)
                                .build(),
                        PageRequest.of(0, 10)
                );

        assertEquals(1, response.getTotalElements());
        assertEquals("Read", response.getContent().get(0).getTitle());
    }

    @Test
    void getUnreadNotificationCount_shouldReturnCorrectCount() {

        notificationService.createNotification(
                user,
                NotificationType.BUDGET_EXCEEDED,
                "One",
                "One",
                UUID.randomUUID()
        );

        NotificationResponse second =
                notificationService.createNotification(
                        user,
                        NotificationType.SETTLEMENT_RECEIVED,
                        "Two",
                        "Two",
                        UUID.randomUUID()
                );

        notificationService.markAsRead(second.getId());

        assertEquals(
                1,
                notificationService.getUnreadNotificationCount()
        );
    }

    @Test
    void markAsRead_shouldMarkNotificationAsRead() {

        NotificationResponse response =
                notificationService.createNotification(
                        user,
                        NotificationType.BUDGET_EXCEEDED,
                        "Budget",
                        "Budget exceeded",
                        UUID.randomUUID()
                );

        assertFalse(
                notificationRepository.findById(response.getId())
                        .orElseThrow()
                        .isRead()
        );

        notificationService.markAsRead(response.getId());

        assertTrue(
                notificationRepository.findById(response.getId())
                        .orElseThrow()
                        .isRead()
        );
    }

    @Test
    void markAsRead_shouldRejectNotificationBelongingToAnotherUser() {

        NotificationResponse response =
                notificationService.createNotification(
                        otherUser,
                        NotificationType.BUDGET_EXCEEDED,
                        "Other",
                        "Other user's notification",
                        UUID.randomUUID()
                );

        assertThrows(
                NotificationDoesNotExistException.class,
                () -> notificationService.markAsRead(response.getId())
        );

        assertFalse(
                notificationRepository.findById(response.getId())
                        .orElseThrow()
                        .isRead()
        );
    }

    @Test
    void markAsRead_shouldRejectNonExistingNotification() {

        assertThrows(
                NotificationDoesNotExistException.class,
                () -> notificationService.markAsRead(UUID.randomUUID())
        );
    }

    @Test
    void markAllAsRead_shouldMarkOnlyCurrentUserNotifications() {

        notificationService.createNotification(
                user,
                NotificationType.BUDGET_EXCEEDED,
                "Mine 1",
                "Mine",
                UUID.randomUUID()
        );

        notificationService.createNotification(
                user,
                NotificationType.SETTLEMENT_RECEIVED,
                "Mine 2",
                "Mine",
                UUID.randomUUID()
        );

        notificationService.createNotification(
                otherUser,
                NotificationType.SETTLEMENT_RECEIVED,
                "Other",
                "Other",
                UUID.randomUUID()
        );

        notificationService.markAllAsRead();

        assertEquals(0, notificationService.getUnreadNotificationCount());

        setAuthenticatedUser(otherUser);

        assertEquals(1, notificationService.getUnreadNotificationCount());
    }

    @Test
    void getMyNotifications_shouldRejectPageSizeAbove100() {

        assertThrows(
                InvalidPaginationException.class,
                () -> notificationService.getMyNotifications(
                        GetNotificationRequest.builder().build(),
                        PageRequest.of(0, 101)
                )
        );
    }

    @Test
    void getMyNotifications_shouldAllowCreatedAtSorting() {

        notificationService.createNotification(
                user,
                NotificationType.BUDGET_EXCEEDED,
                "First",
                "First",
                UUID.randomUUID()
        );

        notificationService.createNotification(
                user,
                NotificationType.SETTLEMENT_RECEIVED,
                "Second",
                "Second",
                UUID.randomUUID()
        );

        NotificationPageResponse response =
                notificationService.getMyNotifications(
                        GetNotificationRequest.builder().build(),
                        PageRequest.of(
                                0,
                                10,
                                Sort.by(Sort.Direction.ASC, "createdAt")
                        )
                );

        assertEquals(2, response.getTotalElements());
        assertEquals("First", response.getContent().get(0).getTitle());
    }

    private User createUser(String prefix) {

        User user = User.builder()
                .username(prefix + "_" + UUID.randomUUID())
                .name("Notification Test User")
                .email(prefix + "_" + UUID.randomUUID() + "@test.com")
                .phoneNumber(null)
                .role(Role.USER)
                .emailVerified(true)
                .build();

        user.updatePasswordHash("test-password-hash");

        return userRepository.save(user);
    }

    private void setAuthenticatedUser(User user) {

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