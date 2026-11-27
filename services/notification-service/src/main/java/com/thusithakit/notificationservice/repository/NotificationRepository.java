package com.thusithakit.notificationservice.repository;

import com.thusithakit.notificationservice.entity.Notification;
import com.thusithakit.notificationservice.entity.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository
    extends JpaRepository<Notification, UUID> {

  Page<Notification> findAllByUserIdOrderByCreatedAtDesc(
      String userId,
      Pageable pageable
  );

  Page<Notification> findAllByUserIdAndStatusOrderByCreatedAtDesc(
      String userId,
      NotificationStatus status,
      Pageable pageable
  );

  long countByUserIdAndStatus(
      String userId,
      NotificationStatus status
  );

  Optional<Notification> findByIdAndUserId(
      UUID id,
      String userId
  );

  @Modifying
  @Query("""
    UPDATE Notification n
    SET n.status = :status,
        n.readAt = :readAt
    WHERE n.userId = :userId
      AND n.status = :unreadStatus
    """)
  int markAllAsRead(
      @Param("userId") String userId,
      @Param("readAt") Instant readAt,
      @Param("status") NotificationStatus status,
      @Param("unreadStatus") NotificationStatus unreadStatus
  );

  long countByUserIdAndStatusNot(
      String userId,
      NotificationStatus status
  );
}