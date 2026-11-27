package com.thusithakit.notificationservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "inbox_events")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InboxEvent {

  @Id
  @Column(name = "event_id", nullable = false)
  private UUID eventId;

  @Column(name = "event_type", nullable = false, length = 150)
  private String eventType;

  @Column(name = "aggregate_id", nullable = false)
  private UUID aggregateId;

  @Column(name = "consumer_name", nullable = false, length = 150)
  private String consumerName;

  @Column(name = "processed_at", nullable = false)
  private Instant processedAt;
}