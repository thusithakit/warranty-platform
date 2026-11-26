package com.thusithakit.purchaseservice.messaging;

import com.thusithakit.purchaseservice.entity.OutboxEvent;
import com.thusithakit.purchaseservice.service.OutboxClaimService;
import com.thusithakit.purchaseservice.service.OutboxStatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

@Component
public class OutboxPublisher {

  private static final Logger log =
      LoggerFactory.getLogger(OutboxPublisher.class);

  private final OutboxClaimService claimService;
  private final OutboxStatusService statusService;
  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;

  @Value("${app.kafka.topics.purchase-events}")
  private String topic;

  public OutboxPublisher(
      OutboxClaimService claimService,
      OutboxStatusService statusService,
      KafkaTemplate<String, String> kafkaTemplate,
      ObjectMapper objectMapper
  ) {
    this.claimService = claimService;
    this.statusService = statusService;
    this.kafkaTemplate = kafkaTemplate;
    this.objectMapper = objectMapper;
  }

  @Scheduled(
      fixedDelayString = "${app.outbox.poll-interval-ms:1000}"
  )
  public void publishPendingEvents() {

    try {
      int recovered = claimService.releaseExpiredEvents();

      if (recovered > 0) {
        log.warn(
            "Recovered {} expired outbox claims",
            recovered
        );
      }

      List<OutboxEvent> events =
          claimService.claimPendingEvents();

      if (events.isEmpty()) {
        return;
      }

      log.info(
          "Claimed {} outbox events",
          events.size()
      );

      for (OutboxEvent event : events) {
        publishEvent(event);
      }

    } catch (Exception exception) {
      log.error(
          "Error while processing outbox events",
          exception
      );
    }
  }

  private void publishEvent(OutboxEvent event) {

    Instant claimTime = event.getLockedAt();

    try {
      EventEnvelope envelope = new EventEnvelope(
          event.getId(),
          event.getEventType(),
          event.getEventVersion(),
          event.getAggregateType(),
          event.getAggregateId(),
          event.getOccurredAt(),
          event.getPayload()
      );

      String message = objectMapper.writeValueAsString(envelope);

      kafkaTemplate.send(
              topic,
              event.getAggregateId().toString(),
              message
          )
          .orTimeout(30, TimeUnit.SECONDS)
          .whenComplete((result, exception) -> {

            if (exception == null) {
              try {
                statusService.markPublished(
                    event.getId(),
                    claimTime
                );

                log.info(
                    "Published outbox event {} to topic {}",
                    event.getId(),
                    topic
                );

              } catch (Exception statusException) {
                log.error(
                    "Kafka acknowledged event {}, " +
                        "but its outbox status could not be updated",
                    event.getId(),
                    statusException
                );
              }

            } else {
              Throwable cause = exception;

              while (cause instanceof CompletionException
                  && cause.getCause() != null) {
                cause = cause.getCause();
              }

              try {
                statusService.scheduleRetry(
                    event.getId(),
                    claimTime,
                    cause
                );

                log.warn(
                    "Failed to publish event {}. Retry scheduled.",
                    event.getId(),
                    cause
                );

              } catch (Exception statusException) {
                log.error(
                    "Could not update failed event {}",
                    event.getId(),
                    statusException
                );
              }
            }
          });

    } catch (Exception exception) {
      try {
        statusService.scheduleRetry(
            event.getId(),
            claimTime,
            exception
        );
      } catch (Exception statusException) {
        log.error(
            "Could not schedule retry for event {}",
            event.getId(),
            statusException
        );
      }
    }
  }
}
