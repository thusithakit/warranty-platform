package com.thusithakit.purchaseservice.messaging;

import com.thusithakit.purchaseservice.entity.OutboxEvent;
import com.thusithakit.purchaseservice.repository.OutboxEventRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class OutboxPublisher {

  private final OutboxEventRepository repository;

  @Value("${app.kafka.topics.purchase-events}")
  private String topic;

  public OutboxPublisher(
      OutboxEventRepository repository
  ) {
    this.repository = repository;
  }

  @Scheduled(
      fixedDelayString = "${app.outbox.poll-interval-ms:1000}"
  )
  public void publishPendingEvents() {

    // Intentionally delegated to a transactional worker in the next step.
    // Do not hold a database transaction open while waiting for Kafka.
  }

  @Transactional
  public void markPublished(OutboxEvent event) {
    event.setStatus("PUBLISHED");
    event.setPublishedAt(Instant.now());

    repository.save(event);
  }
}
