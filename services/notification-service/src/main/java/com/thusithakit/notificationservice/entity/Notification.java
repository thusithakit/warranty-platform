package com.thusithakit.notificationservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "notifications",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_notifications_source_event",
            columnNames = "source_event_id"
        )
    },
    indexes = {
        @Index(
            name = "idx_notifications_user_created",
            columnList = "user_id, created_at"
        )
    }
)
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Notification {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "user_id", nullable = false, length = 255)
  private String userId;

  @Column(name = "source_event_id", nullable = false, unique = true)
  private UUID sourceEventId;

  @Enumerated(EnumType.STRING)
  @Column(name = "notification_type", nullable = false, length = 100)
  private NotificationType notificationType;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String message;

  @Column(name = "resource_type", length = 100)
  private String resourceType;

  @Column(name = "resource_id")
  private UUID resourceId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private NotificationStatus status = NotificationStatus.UNREAD;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "read_at")
  private Instant readAt;

  public Notification(
      String userId,
      UUID sourceEventId,
      NotificationType notificationType,
      String title,
      String message,
      String resourceType,
      UUID resourceId
  ) {
    this.userId = userId;
    this.sourceEventId = sourceEventId;
    this.notificationType = notificationType;
    this.title = title;
    this.message = message;
    this.resourceType = resourceType;
    this.resourceId = resourceId;
    this.status = NotificationStatus.UNREAD;
    this.createdAt = Instant.now();
  }

  public void markAsRead() {
    if (this.status == NotificationStatus.UNREAD) {
      this.status = NotificationStatus.READ;
      this.readAt = Instant.now();
    }
  }

  public void archive() {
    this.status = NotificationStatus.ARCHIVED;
  }
}