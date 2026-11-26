package com.thusithakit.purchaseservice.entity;

import tools.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OutboxEvent {

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
  @Builder.Default
  private Integer eventVersion = 1;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb", nullable = false)
  private JsonNode payload;

  @Column(name = "occurred_at", nullable = false)
  private Instant occurredAt;

  @Column(name = "published_at")
  private Instant publishedAt;

  @Column(nullable = false)
  @Builder.Default
  private String status = "PENDING";

  @Column(nullable = false)
  @Builder.Default
  private Integer attempts = 0;

  @Column(name = "next_attempt_at", nullable = false)
  private Instant nextAttemptAt;

  @Column(name = "last_error")
  private String lastError;

  @Column(name = "locked_at")
  private Instant lockedAt;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @PrePersist
  void onCreate() {
    Instant now = Instant.now();

    if (occurredAt == null) {
      occurredAt = now;
    }

    if (createdAt == null) {
      createdAt = now;
    }

    if (nextAttemptAt == null) {
      nextAttemptAt = now;
    }

    if (status == null) {
      status = "PENDING";
    }

    if (attempts == null) {
      attempts = 0;
    }

    if (eventVersion == null) {
      eventVersion = 1;
    }
  }
}
