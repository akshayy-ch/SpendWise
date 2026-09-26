package com.spendwise.dto.response.notification;

import com.spendwise.enums.NotificationType;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private UUID id;
    private NotificationType type;
    private String title;
    private String message;
    private UUID referenceId;
    private boolean isRead;
    private OffsetDateTime createdAt;
}