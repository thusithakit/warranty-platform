package com.thusithakit.warrantyservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "warranty_outbox_events")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WarrantyOutboxEvent {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "aggregate_type", nullable = false)
  private String aggregateType;

  @Column(name = "aggregate_id", nullable = false)
  private UUID aggregateId;

  @Column(name = "event_type", nullable = false)
  private String eventType;

  @Column(name = "event_version", nullable = false)
  private Integer eventVersion = 1;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb", nullable = false)
  private JsonNode payload;

  @Column(name = "occurred_at", nullable = false)
  private Instant occurredAt;

  @Column(name = "published_at")
  private Instant publishedAt;

  @Column(nullable = false)
  private String status = "PENDING";

  @Column(nullable = false)
  private Integer attempts = 0;

  @Column(name = "next_attempt_at", nullable = false)
  private Instant nextAttemptAt;

  @Column(name = "last_error")
  private String lastError;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "locked_at")
  private Instant lockedAt;

  public WarrantyOutboxEvent(
      UUID aggregateId,
      String eventType,
      JsonNode payload,
      Instant occurredAt
  ) {
    this.aggregateType = "WARRANTY";
    this.aggregateId = aggregateId;
    this.eventType = eventType;
    this.eventVersion = 1;
    this.payload = payload;
    this.occurredAt = occurredAt;
    this.status = "PENDING";
    this.attempts = 0;
    this.nextAttemptAt = occurredAt;
    this.createdAt = occurredAt;
  }
}