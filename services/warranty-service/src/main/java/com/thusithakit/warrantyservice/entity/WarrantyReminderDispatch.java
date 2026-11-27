package com.thusithakit.warrantyservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
    name = "warranty_reminder_dispatches",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_warranty_reminder",
            columnNames = {"warranty_id", "reminder_days_before"}
        )
    }
)
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WarrantyReminderDispatch {

  public enum Status {
    PENDING,
    DISPATCHED,
    CANCELLED
  }

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "warranty_id", nullable = false)
  private UUID warrantyId;

  @Column(name = "user_id", nullable = false)
  private String userId;

  @Column(name = "purchase_id", nullable = false)
  private UUID purchaseId;

  @Column(name = "reminder_days_before", nullable = false)
  private Integer reminderDaysBefore;

  @Column(name = "scheduled_for", nullable = false)
  private LocalDate scheduledFor;

  @Column(name = "dispatched_at")
  private Instant dispatchedAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private Status status = Status.PENDING;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @PrePersist
  protected void onCreate() {
    createdAt = Instant.now();
  }
}