package com.thusithakit.warrantyservice.service;

import com.thusithakit.warrantyservice.entity.WarrantyOutboxEvent;
import com.thusithakit.warrantyservice.messaging.ClaimedWarrantyOutboxEvent;
import com.thusithakit.warrantyservice.repository.WarrantyOutboxEventRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class WarrantyOutboxClaimService {

  private final WarrantyOutboxEventRepository repository;

  @Value("${app.outbox.batch-size:20}")
  private int batchSize;

  @Value("${app.outbox.lease-duration-seconds:120}")
  private long leaseDurationSeconds;

  public WarrantyOutboxClaimService(
      WarrantyOutboxEventRepository repository
  ) {
    this.repository = repository;
  }

  @Transactional
  public List<ClaimedWarrantyOutboxEvent> claimEvents() {

    Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);

    int recovered = repository.recoverExpiredClaims(
        now.minusSeconds(leaseDurationSeconds)
    );

    List<WarrantyOutboxEvent> events =
        repository.findPendingForUpdate(now, batchSize);

    return events.stream()
        .map(event -> {
          event.setStatus("PROCESSING");
          event.setLockedAt(now);

          return new ClaimedWarrantyOutboxEvent(
              event.getId(),
              event.getAggregateId(),
              event.getEventType(),
              event.getPayload().toString(),
              now
          );
        })
        .toList();
  }
}
