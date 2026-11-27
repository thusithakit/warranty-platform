package com.thusithakit.notificationservice.service;

import com.thusithakit.notificationservice.dto.NotificationResponse;
import com.thusithakit.notificationservice.entity.Notification;
import com.thusithakit.notificationservice.entity.NotificationStatus;
import com.thusithakit.notificationservice.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

@Service
public class NotificationManagementService {

  private final NotificationRepository repository;

  public NotificationManagementService(
      NotificationRepository repository
  ) {
    this.repository = repository;
  }

  @Transactional(readOnly = true)
  public Page<NotificationResponse> findAll(
      String userId,
      int page,
      int size
  ) {
    validatePagination(page, size);

    return repository.findAllByUserIdOrderByCreatedAtDesc(
        userId,
        PageRequest.of(
            page,
            size,
            Sort.by(Sort.Direction.DESC, "createdAt")
        )
    ).map(this::toResponse);
  }

  @Transactional(readOnly = true)
  public Page<NotificationResponse> findUnread(
      String userId,
      int page,
      int size
  ) {
    validatePagination(page, size);

    return repository
        .findAllByUserIdAndStatusOrderByCreatedAtDesc(
            userId,
            NotificationStatus.UNREAD,
            PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
            )
        )
        .map(this::toResponse);
  }

  @Transactional(readOnly = true)
  public long getUnreadCount(String userId) {
    return repository.countByUserIdAndStatus(
        userId,
        NotificationStatus.UNREAD
    );
  }

  @Transactional
  public void markAsRead(String userId, UUID notificationId) {

    Notification notification = findOwnedNotification(
        userId,
        notificationId
    );

    notification.markAsRead();
  }

  @Transactional
  public int markAllAsRead(String userId) {
    return repository.markAllAsRead(
        userId,
        Instant.now(),
        NotificationStatus.READ,
        NotificationStatus.UNREAD
    );
  }

  @Transactional
  public void archive(String userId, UUID notificationId) {

    Notification notification = findOwnedNotification(
        userId,
        notificationId
    );

    notification.archive();
  }

  private Notification findOwnedNotification(
      String userId,
      UUID notificationId
  ) {
    return repository.findByIdAndUserId(
        notificationId,
        userId
    ).orElseThrow(() ->
        new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Notification not found"
        )
    );
  }

  private NotificationResponse toResponse(Notification notification) {

    return new NotificationResponse(
        notification.getId(),
        notification.getNotificationType(),
        notification.getTitle(),
        notification.getMessage(),
        notification.getResourceType(),
        notification.getResourceId(),
        notification.getStatus(),
        notification.getCreatedAt(),
        notification.getReadAt()
    );
  }

  private void validatePagination(int page, int size) {
    if (page < 0 || size < 1 || size > 100) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Invalid pagination parameters. Page must be non-negative and size must be between 1 and 100."
      );
    }
  }
}