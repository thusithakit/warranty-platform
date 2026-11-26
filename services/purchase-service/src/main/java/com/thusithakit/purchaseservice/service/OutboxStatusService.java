package com.thusithakit.purchaseservice.service;

import com.thusithakit.purchaseservice.entity.OutboxEvent;
import com.thusithakit.purchaseservice.repository.OutboxEventRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class OutboxStatusService {

  private final OutboxEventRepository repository;

  @Value("${app.outbox.max-attempts:10}")
  private int maxAttempts;

  public OutboxStatusService(
      OutboxEventRepository repository
  ) {
    this.repository = repository;
  }

  @Transactional
  public void markPublished(UUID eventId, Instant claimTime) {

    OutboxEvent event = getClaimedEvent(eventId, claimTime);

    event.setStatus("PUBLISHED");
    event.setPublishedAt(Instant.now());
    event.setLockedAt(null);
    event.setLastError(null);
  }

  @Transactional
  public void scheduleRetry(
      UUID eventId,
      Instant claimTime,
      Throwable exception
  ) {

    OutboxEvent event = getClaimedEvent(eventId, claimTime);

    int attempts = event.getAttempts() + 1;
    event.setAttempts(attempts);
    event.setLastError(truncateError(exception));

    event.setLockedAt(null);

    if (attempts >= maxAttempts) {
      event.setStatus("FAILED");
      return;
    }

    event.setStatus("PENDING");

    long delaySeconds = calculateRetryDelay(attempts);

    event.setNextAttemptAt(
        Instant.now().plusSeconds(delaySeconds)
    );
  }

  private OutboxEvent getClaimedEvent(
      UUID eventId,
      Instant claimTime
  ) {

    OutboxEvent event = repository.findById(eventId)
        .orElseThrow(() ->
            new IllegalStateException(
                "Outbox event not found: " + eventId
            )
        );

    // Prevent an outdated publisher from modifying
    // an event that has already been reclaimed.
    if (!"PROCESSING".equals(event.getStatus())
        || event.getLockedAt() == null
        || claimTime == null
        || !claimTime.truncatedTo(ChronoUnit.MICROS)
        .equals(event.getLockedAt().truncatedTo(ChronoUnit.MICROS))) {

      throw new IllegalStateException(
          "Outbox event is no longer owned by this claim: "
              + eventId
      );
    }

    return event;
  }

  private long calculateRetryDelay(int attempts) {

    // 2, 4, 8, 16 ... up to 900 seconds.
    int exponent = Math.min(attempts, 10);

    return Math.min(
        1L << exponent,
        900L
    );
  }

  private String truncateError(Throwable exception) {

    String message = exception.getMessage();

    if (message == null || message.isBlank()) {
      message = exception.getClass().getSimpleName();
    }

    return message.length() > 2000
        ? message.substring(0, 2000)
        : message;
  }
}
