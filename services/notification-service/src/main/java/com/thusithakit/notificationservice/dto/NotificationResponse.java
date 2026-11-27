package com.thusithakit.notificationservice.dto;

import com.thusithakit.notificationservice.entity.NotificationStatus;
import com.thusithakit.notificationservice.entity.NotificationType;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
    UUID id,
    NotificationType notificationType,
    String title,
    String message,
    String resourceType,
    UUID resourceId,
    NotificationStatus status,
    Instant createdAt,
    Instant readAt
) {
}