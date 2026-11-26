package com.thusithakit.warrantyservice.service;

import com.thusithakit.warrantyservice.entity.InboxEvent;
import com.thusithakit.warrantyservice.entity.PurchaseReference;
import com.thusithakit.warrantyservice.messaging.EventEnvelope;
import com.thusithakit.warrantyservice.messaging.PurchaseCreatedData;
import com.thusithakit.warrantyservice.repository.InboxEventRepository;
import com.thusithakit.warrantyservice.repository.PurchaseReferenceRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PurchaseEventService {

  private static final String CONSUMER_NAME = "warranty-service";

  private final InboxEventRepository inboxEventRepository;
  private final PurchaseReferenceRepository purchaseReferenceRepository;

  @Transactional
  public void processPurchaseCreated(
      EventEnvelope envelope,
      PurchaseCreatedData data
  ) {
    UUID eventId = envelope.eventId();

    // Idempotency: ignore events already processed.
    if (inboxEventRepository.existsById(eventId)) {
      return;
    }

    if (!"PurchaseCreated".equals(envelope.eventType())) {
      throw new IllegalArgumentException(
          "Unsupported event type: " + envelope.eventType()
      );
    }

    if (!envelope.aggregateId().equals(data.purchaseId())) {
      throw new IllegalArgumentException(
          "Event aggregate ID does not match purchase ID"
      );
    }

    // Store the event in the inbox.
    InboxEvent inboxEvent = new InboxEvent();
    inboxEvent.setEventId(eventId);
    inboxEvent.setEventType(envelope.eventType());
    inboxEvent.setAggregateId(envelope.aggregateId());
    inboxEvent.setConsumerName(CONSUMER_NAME);

    inboxEventRepository.save(inboxEvent);

    // Update or create the local purchase projection.
    PurchaseReference reference =
        purchaseReferenceRepository
            .findById(data.purchaseId())
            .orElseGet(PurchaseReference::new);

    reference.setPurchaseId(data.purchaseId());
    reference.setUserId(data.userId());
    reference.setItemName(data.itemName());
    reference.setPurchaseDate(data.purchaseDate());
    reference.setSourceEventId(eventId);
    reference.setUpdatedAt(Instant.now());

    purchaseReferenceRepository.save(reference);
  }
}