package com.thusithakit.warrantyservice.service;

import com.thusithakit.warrantyservice.entity.Warranty;
import com.thusithakit.warrantyservice.entity.WarrantyOutboxEvent;
import com.thusithakit.warrantyservice.entity.WarrantyStatus;
import com.thusithakit.warrantyservice.messaging.WarrantyExpiredData;
import com.thusithakit.warrantyservice.repository.WarrantyOutboxEventRepository;
import com.thusithakit.warrantyservice.repository.WarrantyRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
public class WarrantyExpiryService {

  private static final ZoneId BUSINESS_ZONE =
      ZoneId.of("Asia/Colombo");

  private final WarrantyRepository warrantyRepository;
  private final WarrantyOutboxEventRepository outboxRepository;
  private final ObjectMapper objectMapper;

  @Value("${app.expiry.batch-size:100}")
  private int batchSize;

  public WarrantyExpiryService(
      WarrantyRepository warrantyRepository,
      WarrantyOutboxEventRepository outboxRepository,
      ObjectMapper objectMapper
  ) {
    this.warrantyRepository = warrantyRepository;
    this.outboxRepository = outboxRepository;
    this.objectMapper = objectMapper;
  }

  @Transactional
  public int expireWarranties() {

    LocalDate today = LocalDate.now(BUSINESS_ZONE);

    List<Warranty> expiredWarranties =
        warrantyRepository.findExpiredWarrantiesForUpdate(
            today,
            batchSize
        );

    for (Warranty warranty : expiredWarranties) {

      warranty.setStatus(WarrantyStatus.EXPIRED);

      Instant occurredAt = Instant.now();

      WarrantyExpiredData data = new WarrantyExpiredData(
          warranty.getId(),
          warranty.getPurchaseId(),
          warranty.getUserId(),
          warranty.getEndDate()
      );

      UUID eventId = UUID.randomUUID();

      var eventEnvelope = new WarrantyExpiredEventEnvelope(
          eventId,
          "WarrantyExpired",
          1,
          "WARRANTY",
          warranty.getId(),
          occurredAt,
          data
      );

      WarrantyOutboxEvent outboxEvent =
          new WarrantyOutboxEvent(
              warranty.getId(),
              "WarrantyExpired",
              objectMapper.valueToTree(eventEnvelope),
              occurredAt
          );

      outboxRepository.save(outboxEvent);
    }

    return expiredWarranties.size();
  }

  public record WarrantyExpiredEventEnvelope(
      UUID eventId,
      String eventType,
      Integer eventVersion,
      String aggregateType,
      UUID aggregateId,
      Instant occurredAt,
      WarrantyExpiredData data
  ) {
  }
}