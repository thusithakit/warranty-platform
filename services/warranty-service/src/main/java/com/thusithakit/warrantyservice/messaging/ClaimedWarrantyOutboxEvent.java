package com.thusithakit.warrantyservice.messaging;

import java.time.Instant;
import java.util.UUID;

public record ClaimedWarrantyOutboxEvent(
    UUID id,
    UUID aggregateId,
    String eventType,
    String payload,
    Instant claimTime
) {
}