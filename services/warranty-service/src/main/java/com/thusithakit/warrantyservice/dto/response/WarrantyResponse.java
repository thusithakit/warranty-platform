package com.thusithakit.warrantyservice.dto.response;

import com.thusithakit.warrantyservice.entity.WarrantyStatus;
import com.thusithakit.warrantyservice.entity.WarrantyType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record WarrantyResponse(
    UUID id,
    UUID purchaseId,
    String warrantyProvider,
    String warrantyReference,
    WarrantyType warrantyType,
    LocalDate startDate,
    LocalDate endDate,
    String coverageDescription,
    WarrantyStatus status,
    Instant createdAt,
    Instant updatedAt
) {
}
