package com.thusithakit.warrantyservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "warranties")
@Getter
@Setter
@NoArgsConstructor
public class Warranty {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private String userId;

  @Column(name = "purchase_id", nullable = false)
  private UUID purchaseId;

  @Column(name = "warranty_provider")
  private String warrantyProvider;

  @Column(name = "warranty_reference")
  private String warrantyReference;

  @Column(name = "warranty_type", nullable = false)
  @Enumerated(EnumType.STRING)
  private WarrantyType warrantyType;

  @Column(name = "start_date")
  private LocalDate startDate;

  @Column(name = "end_date")
  private LocalDate endDate;

  @Column(name = "coverage_description")
  private String coverageDescription;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private WarrantyStatus status = WarrantyStatus.ACTIVE;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Version
  private Long version;

  @PrePersist
  protected void onCreate() {
    Instant now = Instant.now();
    createdAt = now;
    updatedAt = now;
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = Instant.now();
  }
}
