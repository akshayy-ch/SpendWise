package com.spendwise.controller;

import com.spendwise.dto.request.notification.GetNotificationRequest;
import com.spendwise.dto.response.notification.NotificationPageResponse;
import com.spendwise.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<NotificationPageResponse> getMyNotifications(GetNotificationRequest request, Pageable pageable) {
        return ResponseEntity.ok(notificationService.getMyNotifications(request, pageable));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadNotificationCount() {
        return ResponseEntity.ok(notificationService.getUnreadNotificationCount());
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.noContent().build();
    }
}