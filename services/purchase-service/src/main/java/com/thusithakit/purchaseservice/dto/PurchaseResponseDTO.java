package com.thusithakit.purchaseservice.dto;

import com.thusithakit.purchaseservice.entity.PurchaseStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PurchaseResponseDTO(
    UUID id,
    String itemName,
    String brand,
    String model,
    BigDecimal purchasePrice,
    String currency,
    String sellerName,
    LocalDate purchaseDate,
    String notes,
    PurchaseStatus status,
    Instant createdAt,
    Instant updatedAt
) {}