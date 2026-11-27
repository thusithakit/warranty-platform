package com.thusithakit.notificationservice.service;

import com.thusithakit.notificationservice.entity.Notification;
import com.thusithakit.notificationservice.entity.NotificationType;
import com.thusithakit.notificationservice.messaging.EventEnvelope;
import com.thusithakit.notificationservice.messaging.WarrantyExpiredData;
import com.thusithakit.notificationservice.messaging.WarrantyExpiringSoonData;
import com.thusithakit.notificationservice.repository.InboxEventRepository;
import com.thusithakit.notificationservice.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class WarrantyNotificationService {

  private static final Logger log =
      LoggerFactory.getLogger(WarrantyNotificationService.class);

  private static final String CONSUMER_NAME =
      "notification-service";

  private final InboxEventRepository inboxRepository;
  private final NotificationRepository notificationRepository;

  public WarrantyNotificationService(
      InboxEventRepository inboxRepository,
      NotificationRepository notificationRepository
  ) {
    this.inboxRepository = inboxRepository;
    this.notificationRepository = notificationRepository;
  }

  @Transactional
  public void processWarrantyExpired(
      EventEnvelope<WarrantyExpiredData> event
  ) {
    validateExpired(event);

    WarrantyExpiredData data = event.data();

    if (!registerEvent(event.eventId(), event.eventType(),
        event.aggregateId())) {
      return;
    }

    String title = "Warranty expired";

    String message = data.endDate() == null
        ? "One of your registered warranties has expired."
        : "One of your registered warranties expired on "
        + data.endDate() + ".";

    Notification notification = new Notification(
        data.userId(),
        event.eventId(),
        NotificationType.WARRANTY_EXPIRED,
        title,
        message,
        "WARRANTY",
        data.warrantyId()
    );

    notificationRepository.save(notification);

    log.info(
        "Created warranty expiry notification: eventId={}, userId={}, warrantyId={}",
        event.eventId(),
        data.userId(),
        data.warrantyId()
    );
  }

  @Transactional
  public void processWarrantyExpiringSoon(
      EventEnvelope<WarrantyExpiringSoonData> event
  ) {
    validateExpiringSoon(event);

    WarrantyExpiringSoonData data = event.data();

    if (!registerEvent(event.eventId(), event.eventType(),
        event.aggregateId())) {
      return;
    }

    String title = data.daysBefore() == 1
        ? "Warranty expires tomorrow"
        : "Warranty expiring soon";

    String message = data.daysBefore() == 1
        ? "One of your warranties expires tomorrow ("
        + data.endDate() + ")."
        : "One of your warranties expires in "
        + data.daysBefore() + " days ("
        + data.endDate() + ").";

    Notification notification = new Notification(
        data.userId(),
        event.eventId(),
        NotificationType.WARRANTY_EXPIRING_SOON,
        title,
        message,
        "WARRANTY",
        data.warrantyId()
    );

    notificationRepository.save(notification);

    log.info(
        "Created warranty expiring notification: eventId={}, userId={}, warrantyId={}, daysBefore={}",
        event.eventId(),
        data.userId(),
        data.warrantyId(),
        data.daysBefore()
    );
  }

  private boolean registerEvent(
      java.util.UUID eventId,
      String eventType,
      java.util.UUID aggregateId
  ) {
    int inserted = inboxRepository.insertIfNotProcessed(
        eventId,
        eventType,
        aggregateId,
        CONSUMER_NAME
    );

    if (inserted == 0) {
      log.info("Ignoring duplicate event: eventId={}", eventId);
      return false;
    }

    return true;
  }

  private void validateExpired(
      EventEnvelope<WarrantyExpiredData> event
  ) {
    Objects.requireNonNull(event, "Event must not be null");
    Objects.requireNonNull(event.eventId(), "Event ID is required");
    Objects.requireNonNull(event.aggregateId(), "Aggregate ID is required");
    Objects.requireNonNull(event.data(), "Event data is required");

    WarrantyExpiredData data = event.data();

    if (!"WarrantyExpired".equals(event.eventType())) {
      throw new IllegalArgumentException("Unsupported event type");
    }

    validateEnvelope(event.eventVersion(), event.aggregateType());

    Objects.requireNonNull(data.warrantyId(), "Warranty ID is required");
    Objects.requireNonNull(data.userId(), "User ID is required");

    if (data.userId().isBlank()) {
      throw new IllegalArgumentException("User ID must not be blank");
    }

    if (!event.aggregateId().equals(data.warrantyId())) {
      throw new IllegalArgumentException(
          "Event aggregate ID does not match warranty ID"
      );
    }
  }

  private void validateExpiringSoon(
      EventEnvelope<WarrantyExpiringSoonData> event
  ) {
    Objects.requireNonNull(event, "Event must not be null");
    Objects.requireNonNull(event.eventId(), "Event ID is required");
    Objects.requireNonNull(event.aggregateId(), "Aggregate ID is required");
    Objects.requireNonNull(event.data(), "Event data is required");

    WarrantyExpiringSoonData data = event.data();

    if (!"WarrantyExpiringSoon".equals(event.eventType())) {
      throw new IllegalArgumentException("Unsupported event type");
    }

    validateEnvelope(event.eventVersion(), event.aggregateType());

    Objects.requireNonNull(data.dispatchId(), "Dispatch ID is required");
    Objects.requireNonNull(data.warrantyId(), "Warranty ID is required");
    Objects.requireNonNull(data.userId(), "User ID is required");
    Objects.requireNonNull(data.endDate(), "End date is required");
    Objects.requireNonNull(data.daysBefore(), "Days before is required");

    if (data.userId().isBlank()) {
      throw new IllegalArgumentException("User ID must not be blank");
    }

    if (data.daysBefore() < 1 || data.daysBefore() > 365) {
      throw new IllegalArgumentException("Invalid reminder interval");
    }

    if (!event.aggregateId().equals(data.warrantyId())) {
      throw new IllegalArgumentException(
          "Event aggregate ID does not match warranty ID"
      );
    }
  }

  private void validateEnvelope(
      Integer eventVersion,
      String aggregateType
  ) {
    if (!"WARRANTY".equals(aggregateType)) {
      throw new IllegalArgumentException("Invalid aggregate type");
    }

    if (eventVersion == null || eventVersion != 1) {
      throw new IllegalArgumentException("Unsupported event version");
    }
  }
}