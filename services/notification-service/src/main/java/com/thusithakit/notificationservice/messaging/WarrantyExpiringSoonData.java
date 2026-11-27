package com.thusithakit.notificationservice.messaging;

import java.time.LocalDate;
import java.util.UUID;

public record WarrantyExpiringSoonData(
    UUID dispatchId,
    UUID warrantyId,
    UUID purchaseId,
    String userId,
    LocalDate endDate,
    Integer daysBefore,
    LocalDate scheduledFor
) {}