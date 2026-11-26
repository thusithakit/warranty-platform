package com.thusithakit.purchaseservice.service;

import com.thusithakit.purchaseservice.entity.OutboxEvent;
import com.thusithakit.purchaseservice.repository.OutboxEventRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class OutboxClaimService {

  private final OutboxEventRepository repository;

  @Value("${app.outbox.batch-size:20}")
  private int batchSize;

  @Value("${app.outbox.lease-duration-seconds:120}")
  private long leaseDurationSeconds;

  public OutboxClaimService(
      OutboxEventRepository repository
  ) {
    this.repository = repository;
  }

  @Transactional
  public List<OutboxEvent> claimPendingEvents() {

    Instant now = Instant.now()
        .truncatedTo(ChronoUnit.MICROS);

    List<OutboxEvent> events =
        repository.findPendingEventsForUpdate(
            now,
            batchSize
        );

    for (OutboxEvent event : events) {
      event.setStatus("PROCESSING");
      event.setLockedAt(now);
    }

    return events;
  }

  @Transactional
  public int releaseExpiredEvents() {

    Instant leaseExpiry = Instant.now()
        .minus(leaseDurationSeconds, ChronoUnit.SECONDS);

    return repository.releaseExpiredClaims(leaseExpiry);
  }
}
