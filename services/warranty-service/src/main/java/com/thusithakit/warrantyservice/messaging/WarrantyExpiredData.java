package com.thusithakit.warrantyservice.messaging;

import java.time.LocalDate;
import java.util.UUID;

public record WarrantyExpiredData(
    UUID warrantyId,
    UUID purchaseId,
    String userId,
    LocalDate endDate
) {
}