package com.thusithakit.warrantyservice.messaging;

import com.thusithakit.warrantyservice.service.WarrantyOutboxClaimService;
import com.thusithakit.warrantyservice.service.WarrantyOutboxStatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class WarrantyOutboxPublisher {

  private static final Logger log =
      LoggerFactory.getLogger(WarrantyOutboxPublisher.class);

  private final WarrantyOutboxClaimService claimService;
  private final WarrantyOutboxStatusService statusService;
  private final KafkaTemplate<String, String> kafkaTemplate;

  @Value("${app.kafka.topics.warranty-events:warranty-events}")
  private String warrantyEventsTopic;

  public WarrantyOutboxPublisher(
      WarrantyOutboxClaimService claimService,
      WarrantyOutboxStatusService statusService,
      KafkaTemplate<String, String> kafkaTemplate
  ) {
    this.claimService = claimService;
    this.statusService = statusService;
    this.kafkaTemplate = kafkaTemplate;
  }

  @Scheduled(fixedDelayString = "${app.outbox.poll-interval-ms:1000}")
  public void publishPendingEvents() {

    List<ClaimedWarrantyOutboxEvent> events =
        claimService.claimEvents();

    if (events.isEmpty()) {
      return;
    }

    log.info("Claimed {} warranty outbox events", events.size());

    for (ClaimedWarrantyOutboxEvent event : events) {
      publishEvent(event);
    }
  }

  private void publishEvent(ClaimedWarrantyOutboxEvent event) {

    try {
      kafkaTemplate.send(
          warrantyEventsTopic,
          event.aggregateId().toString(),
          event.payload()
      ).get(30, TimeUnit.SECONDS);

    } catch (InterruptedException exception) {

      Thread.currentThread().interrupt();

      handleFailure(event, exception);
      return;

    } catch (ExecutionException | TimeoutException exception) {

      handleFailure(event, exception);
      return;

    } catch (Exception exception) {

      handleFailure(event, exception);
      return;
    }

    try {
      statusService.markPublished(
          event.id(),
          event.claimTime()
      );

      log.info(
          "Published warranty event: eventId={}, type={}, aggregateId={}",
          event.id(),
          event.eventType(),
          event.aggregateId()
      );

    } catch (Exception exception) {

      // Kafka may already have accepted the message.
      // Leave recovery to the claim lease if status update fails.
      log.error(
          "Kafka publish succeeded but outbox status update failed: eventId={}",
          event.id(),
          exception
      );
    }
  }

  private void handleFailure(
      ClaimedWarrantyOutboxEvent event,
      Exception exception
  ) {
    try {
      statusService.scheduleRetry(
          event.id(),
          event.claimTime(),
          exception
      );

      log.warn(
          "Warranty event publishing failed; retry scheduled: eventId={}, type={}",
          event.id(),
          event.eventType(),
          exception
      );

    } catch (Exception statusException) {

      log.error(
          "Could not update failed warranty outbox event: eventId={}",
          event.id(),
          statusException
      );
    }
  }
}
