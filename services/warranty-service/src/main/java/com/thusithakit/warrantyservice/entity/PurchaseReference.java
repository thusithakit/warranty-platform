package com.thusithakit.warrantyservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "purchase_references")
@Getter
@Setter
@NoArgsConstructor
public class PurchaseReference {

  @Id
  @Column(name = "purchase_id", nullable = false)
  private UUID purchaseId;

  @Column(name = "user_id", nullable = false)
  private String userId;

  @Column(name = "item_name", nullable = false)
  private String itemName;

  @Column(name = "purchase_date")
  private LocalDate purchaseDate;

  @Column(name = "source_event_id", nullable = false)
  private UUID sourceEventId;

  @Column(name = "received_at", nullable = false)
  private Instant receivedAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @PrePersist
  protected void onCreate() {
    Instant now = Instant.now();
    receivedAt = now;
    updatedAt = now;
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = Instant.now();
  }
}
