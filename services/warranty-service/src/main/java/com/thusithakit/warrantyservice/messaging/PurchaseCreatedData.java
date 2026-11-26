package com.thusithakit.warrantyservice.messaging;

import java.time.LocalDate;
import java.util.UUID;

public record PurchaseCreatedData(
    UUID purchaseId,
    String userId,
    String itemName,
    LocalDate purchaseDate
) {
}