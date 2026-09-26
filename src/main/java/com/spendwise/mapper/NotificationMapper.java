package com.spendwise.mapper;

import com.spendwise.dto.response.notification.NotificationResponse;
import com.spendwise.entity.Notification;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    NotificationResponse toResponse(Notification notification);
}
