package com.spendwise.dto.request.notification;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetNotificationRequest {

    private Boolean unread;
}