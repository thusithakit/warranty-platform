package com.thusithakit.purchaseservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "purchases")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Purchase {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private String userId;

  @Column(name = "item_name", nullable = false)
  private String itemName;

  private String brand;

  private String model;

  @Column(name = "purchase_price", precision = 12, scale = 2)
  private BigDecimal purchasePrice;

  @Column(nullable = false, length = 3)
  private String currency = "LKR";

  @Column(name = "seller_name")
  private String sellerName;

  @Column(name = "purchase_date", nullable = false)
  private LocalDate purchaseDate;

  private String notes;

  @Enumerated(EnumType.STRING)
  private PurchaseStatus status = PurchaseStatus.ACTIVE;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Version
  private Long version;

  @PrePersist
  void onCreate() {
    Instant now = Instant.now();
    createdAt = now;
    updatedAt = now;
  }

  @PreUpdate
  void onUpdate() {
    updatedAt = Instant.now();
  }

}
