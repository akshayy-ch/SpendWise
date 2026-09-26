package com.spendwise.service;

import com.spendwise.dto.request.notification.GetNotificationRequest;
import com.spendwise.dto.response.notification.NotificationPageResponse;
import com.spendwise.dto.response.notification.NotificationResponse;
import com.spendwise.entity.Notification;
import com.spendwise.entity.User;
import com.spendwise.enums.NotificationSortField;
import com.spendwise.enums.NotificationType;
import com.spendwise.exception.NotificationExceptions.NotificationDoesNotExistException;
import com.spendwise.exception.PaginationException.InvalidPaginationException;
import com.spendwise.mapper.NotificationMapper;
import com.spendwise.repository.NotificationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final CurrentUserService currentUserService;
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    public NotificationResponse createNotification(User recipient, NotificationType type, String title, String message, UUID referenceId){
        Notification notification = Notification.builder()
                .type(type)
                .title(title)
                .message(message)
                .referenceId(referenceId)
                .recipient(recipient)
                .build();
        Notification savedNotification = notificationRepository.save(notification);
        NotificationResponse response = NotificationResponse.builder()
                .id(savedNotification.getId())
                .type(savedNotification.getType())
                .title(savedNotification.getTitle())
                .message(savedNotification.getMessage())
                .referenceId(savedNotification.getReferenceId())
                .isRead(savedNotification.isRead())
                .createdAt(savedNotification.getCreatedAt())
                .build();
        return response;
    }

    public NotificationPageResponse getMyNotifications(GetNotificationRequest request, Pageable pageable){
        UUID userId = currentUserService.getCurrentUserId();

        Pageable customPageable = buildSafePageable(pageable);

        Page<Notification> notificationPage;

        if (request.getUnread() == null) {notificationPage = notificationRepository.findByRecipientId(userId, customPageable);

        } else if (request.getUnread()) {notificationPage = notificationRepository.findByRecipientIdAndIsReadFalse(userId, customPageable);

        } else {
            notificationPage = notificationRepository.findByRecipientIdAndIsReadTrue(userId, customPageable);
        }
        Page<NotificationResponse> responsePage = notificationPage.map(notificationMapper::toResponse);

        NotificationPageResponse response = NotificationPageResponse.builder()
                .content(responsePage.getContent())
                .page(responsePage.getNumber())
                .size(responsePage.getSize())
                .totalElements(responsePage.getTotalElements())
                .totalPages(responsePage.getTotalPages())
                .first(responsePage.isFirst())
                .last(responsePage.isLast())
                .build();
        return response;
    }

    public long getUnreadNotificationCount(){
        UUID userId = currentUserService.getCurrentUserId();
        return notificationRepository.countByRecipientIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(UUID notificationId) {

        UUID userId = currentUserService.getCurrentUserId();

        Notification notification = notificationRepository.findByIdAndRecipientId(notificationId, userId).orElseThrow(() -> new NotificationDoesNotExistException("Notification not found"));

        notification.setRead(true);

        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead() {

        UUID userId = currentUserService.getCurrentUserId();

        notificationRepository.markAllAsRead(userId);
    }

    private org.springframework.data.domain.Pageable buildSafePageable(org.springframework.data.domain.Pageable pageable) {
        List<Sort.Order> orders = new ArrayList<>();

        int pageSize = pageable.getPageSize();
        if (pageSize > 100) {
            throw new InvalidPaginationException("Page size not valid: " + pageSize);        }
        if (pageable.getSort().isUnsorted()) {
            orders.add(Sort.Order.desc(NotificationSortField.CREATED_AT.getEntityField()));
        } else {
            for (Sort.Order order : pageable.getSort()) {
                NotificationSortField sortField = NotificationSortField.fromApiName(order.getProperty());
                orders.add(new Sort.Order( order.getDirection(), sortField.getEntityField()));
            }
        }

        orders.add(Sort.Order.desc("id"));
        Sort sort = Sort.by(orders);

        return PageRequest.of(pageable.getPageNumber(), pageSize, sort);
    }
}
