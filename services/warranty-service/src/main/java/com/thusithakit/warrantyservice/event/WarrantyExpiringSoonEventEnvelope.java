package com.thusithakit.warrantyservice.event;

import java.time.Instant;
import java.util.UUID;

public record WarrantyExpiringSoonEventEnvelope(
    UUID eventId,
    String eventType,
    Integer eventVersion,
    String aggregateType,
    UUID aggregateId,
    Instant occurredAt,
    WarrantyExpiringSoonData data
) {
}