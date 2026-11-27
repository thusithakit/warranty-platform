package com.thusithakit.warrantyservice.service;

import com.thusithakit.warrantyservice.entity.WarrantyOutboxEvent;
import com.thusithakit.warrantyservice.repository.WarrantyOutboxEventRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class WarrantyOutboxStatusService {

  private final WarrantyOutboxEventRepository repository;

  @Value("${app.outbox.max-attempts:10}")
  private int maxAttempts;

  @Value("${app.outbox.initial-delay-ms:1000}")
  private long initialDelayMs;

  @Value("${app.outbox.max-delay-ms:300000}")
  private long maxDelayMs;

  public WarrantyOutboxStatusService(
      WarrantyOutboxEventRepository repository
  ) {
    this.repository = repository;
  }

  @Transactional
  public void markPublished(
      java.util.UUID eventId,
      Instant claimTime
  ) {
    WarrantyOutboxEvent event = getOwnedEvent(eventId, claimTime);

    Instant now = Instant.now();

    event.setStatus("PUBLISHED");
    event.setPublishedAt(now);
    event.setLockedAt(null);
    event.setLastError(null);
  }

  @Transactional
  public void scheduleRetry(
      java.util.UUID eventId,
      Instant claimTime,
      Exception exception
  ) {
    WarrantyOutboxEvent event = getOwnedEvent(eventId, claimTime);

    int attempts = event.getAttempts() + 1;
    event.setAttempts(attempts);
    event.setLockedAt(null);

    String error = exception.getMessage();

    if (error == null || error.isBlank()) {
      error = exception.getClass().getSimpleName();
    }

    event.setLastError(
        error.length() > 4000
            ? error.substring(0, 4000)
            : error
    );

    if (attempts >= maxAttempts) {
      event.setStatus("FAILED");
      return;
    }

    event.setStatus("PENDING");

    Duration delay = calculateRetryDelay(attempts);

    event.setNextAttemptAt(
        Instant.now().plus(delay)
    );
  }

  private WarrantyOutboxEvent getOwnedEvent(
      java.util.UUID eventId,
      Instant claimTime
  ) {
    WarrantyOutboxEvent event = repository.findById(eventId)
        .orElseThrow(() ->
            new IllegalStateException(
                "Outbox event no longer exists: " + eventId
            )
        );

    Instant storedClaim = event.getLockedAt();

    if (!"PROCESSING".equals(event.getStatus())
        || storedClaim == null
        || !storedClaim.equals(
        claimTime.truncatedTo(ChronoUnit.MICROS)
    )) {
      throw new IllegalStateException(
          "Outbox event is no longer owned by this claim: "
              + eventId
      );
    }

    return event;
  }

  private Duration calculateRetryDelay(int attempts) {

    long exponentialCap = initialDelayMs;

    for (int i = 1; i < attempts && exponentialCap < maxDelayMs; i++) {
      if (exponentialCap > maxDelayMs / 2) {
        exponentialCap = maxDelayMs;
        break;
      }

      exponentialCap *= 2;
    }

    exponentialCap = Math.min(exponentialCap, maxDelayMs);

    // Full jitter: random delay between 0 and the exponential cap.
    long jitteredDelay = ThreadLocalRandom.current()
        .nextLong(exponentialCap + 1);

    return Duration.ofMillis(jitteredDelay);
  }
}
