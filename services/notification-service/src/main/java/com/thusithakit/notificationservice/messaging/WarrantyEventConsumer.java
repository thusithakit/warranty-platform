package com.thusithakit.notificationservice.messaging;

import com.thusithakit.notificationservice.service.WarrantyNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class WarrantyEventConsumer {

  private static final Logger log =
      LoggerFactory.getLogger(WarrantyEventConsumer.class);

  private final ObjectMapper objectMapper;
  private final WarrantyNotificationService notificationService;

  public WarrantyEventConsumer(
      ObjectMapper objectMapper,
      WarrantyNotificationService notificationService
  ) {
    this.objectMapper = objectMapper;
    this.notificationService = notificationService;
  }

  @KafkaListener(
      topics = "${app.kafka.topics.warranty-events:warranty-events}",
      groupId = "${spring.kafka.consumer.group-id}"
  )
  public void consumeWarrantyEvent(String message) {

    try {
      JsonNode root = objectMapper.readTree(message);

      JsonNode eventTypeNode = root.path("eventType");

      if (eventTypeNode.isMissingNode()
          || eventTypeNode.isNull()) {
        throw new IllegalArgumentException(
            "Warranty event is missing eventType"
        );
      }

      String eventType = eventTypeNode.asString();

      switch (eventType) {

        case "WarrantyExpired" -> {
          EventEnvelope<WarrantyExpiredData> event =
              objectMapper.readValue(
                  message,
                  objectMapper.getTypeFactory()
                      .constructParametricType(
                          EventEnvelope.class,
                          WarrantyExpiredData.class
                      )
              );

          notificationService.processWarrantyExpired(event);
        }

        case "WarrantyExpiringSoon" -> {
          EventEnvelope<WarrantyExpiringSoonData> event =
              objectMapper.readValue(
                  message,
                  objectMapper.getTypeFactory()
                      .constructParametricType(
                          EventEnvelope.class,
                          WarrantyExpiringSoonData.class
                      )
              );

          notificationService.processWarrantyExpiringSoon(event);
        }

        default -> log.debug(
            "Ignoring unsupported warranty event: {}",
            eventType
        );
      }

    } catch (Exception exception) {

      log.error(
          "Failed to process warranty event. Message will be retried.",
          exception
      );

      throw new IllegalStateException(
          "Warranty event processing failed",
          exception
      );
    }
  }
}