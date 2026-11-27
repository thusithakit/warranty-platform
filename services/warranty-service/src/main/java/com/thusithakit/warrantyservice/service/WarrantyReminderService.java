package com.thusithakit.warrantyservice.service;

import com.thusithakit.warrantyservice.entity.Warranty;
import com.thusithakit.warrantyservice.entity.WarrantyOutboxEvent;
import com.thusithakit.warrantyservice.entity.WarrantyReminderDispatch;
import com.thusithakit.warrantyservice.event.WarrantyExpiringSoonData;
import com.thusithakit.warrantyservice.event.WarrantyExpiringSoonEventEnvelope;
import com.thusithakit.warrantyservice.repository.WarrantyOutboxEventRepository;
import com.thusithakit.warrantyservice.repository.WarrantyReminderDispatchRepository;
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
public class WarrantyReminderService {

  private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Colombo");

  private final WarrantyRepository warrantyRepository;
  private final WarrantyReminderDispatchRepository dispatchRepository;
  private final WarrantyOutboxEventRepository outboxRepository;
  private final ObjectMapper objectMapper;

  @Value("${app.reminders.default-days:30,7,1}")
  private List<Integer> defaultReminderDays;

  public WarrantyReminderService(
      WarrantyRepository warrantyRepository,
      WarrantyReminderDispatchRepository dispatchRepository,
      WarrantyOutboxEventRepository outboxRepository,
      ObjectMapper objectMapper
  ) {
    this.warrantyRepository = warrantyRepository;
    this.dispatchRepository = dispatchRepository;
    this.outboxRepository = outboxRepository;
    this.objectMapper = objectMapper;
  }

  @Transactional
  public int generateDueReminders() {
    LocalDate today = LocalDate.now(BUSINESS_ZONE);
    LocalDate maximumDate = today.plusDays(365);

    List<Warranty> warranties =
        warrantyRepository.findActiveWarrantiesEndingBetween(
            today,
            maximumDate
        );

    int generatedCount = 0;

    for (Warranty warranty : warranties) {
      for (Integer daysBefore : defaultReminderDays) {

        LocalDate scheduledFor =
            warranty.getEndDate().minusDays(daysBefore);

        // Do not generate reminders before their scheduled date.
        if (scheduledFor.isAfter(today)) {
          continue;
        }

        int inserted = dispatchRepository.insertIfAbsent(
            warranty.getId(),
            warranty.getUserId(),
            warranty.getPurchaseId(),
            daysBefore,
            scheduledFor
        );

        // A reminder for this interval already exists.
        if (inserted == 0) {
          continue;
        }

        createOutboxEvent(warranty, daysBefore, scheduledFor);
        generatedCount++;
      }
    }

    return generatedCount;
  }



  private void createOutboxEvent(
      Warranty warranty,
      Integer daysBefore,
      LocalDate scheduledFor
  ) {
    Instant occurredAt = Instant.now();

    WarrantyReminderDispatch dispatch = dispatchRepository
        .findByWarrantyAndDaysBefore(
            warranty.getId(),
            daysBefore
        )
        .orElseThrow(() -> new IllegalStateException(
            "Reminder dispatch was inserted but could not be retrieved"
        ));

    WarrantyExpiringSoonData data = new WarrantyExpiringSoonData(
        dispatch.getId(),
        warranty.getId(),
        warranty.getPurchaseId(),
        warranty.getUserId(),
        warranty.getEndDate(),
        daysBefore,
        scheduledFor
    );

    WarrantyExpiringSoonEventEnvelope envelope =
        new WarrantyExpiringSoonEventEnvelope(
            UUID.randomUUID(),
            "WarrantyExpiringSoon",
            1,
            "WARRANTY",
            warranty.getId(),
            occurredAt,
            data
        );

    WarrantyOutboxEvent outboxEvent = new WarrantyOutboxEvent(
        warranty.getId(),
        "WarrantyExpiringSoon",
        objectMapper.valueToTree(envelope),
        occurredAt
    );

    outboxRepository.save(outboxEvent);
  }
}