package com.thusithakit.notificationservice.messaging;

import java.time.Instant;
import java.util.UUID;

public record EventEnvelope<T>(
    UUID eventId,
    String eventType,
    Integer eventVersion,
    String aggregateType,
    UUID aggregateId,
    Instant occurredAt,
    T data
) {
}
