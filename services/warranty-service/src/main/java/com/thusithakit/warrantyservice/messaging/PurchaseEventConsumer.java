package com.thusithakit.warrantyservice.messaging;

import com.thusithakit.warrantyservice.service.PurchaseEventService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class PurchaseEventConsumer {

  private final ObjectMapper objectMapper;
  private final PurchaseEventService purchaseEventService;

  @KafkaListener(
      topics = "${app.kafka.topics.purchase-events}",
      groupId = "${spring.kafka.consumer.group-id}"
  )
  public void consume(String message) {

    try {
      EventEnvelope envelope =
          objectMapper.readValue(message, EventEnvelope.class);

      if (!"PurchaseCreated".equals(envelope.eventType())) {
        log.debug(
            "Ignoring unsupported purchase event: {}",
            envelope.eventType()
        );
        return;
      }

      PurchaseCreatedData data =
          objectMapper.convertValue(
              envelope.data(),
              PurchaseCreatedData.class
          );

      purchaseEventService.processPurchaseCreated(envelope, data);

      log.info(
          "Processed PurchaseCreated event. Event ID: {}, Purchase ID: {}",
          envelope.eventId(),
          data.purchaseId()
      );

    } catch (Exception exception) {
      log.error("Failed to process purchase event", exception);

      // Propagate the exception so Kafka does not treat
      // the failed message as successfully processed.
      throw new IllegalStateException(
          "Purchase event processing failed",
          exception
      );
    }
  }
}