package com.thusithakit.warrantyservice.messaging;

import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.UUID;

public record EventEnvelope(
    UUID eventId,
    String eventType,
    Integer eventVersion,
    String aggregateType,
    UUID aggregateId,
    Instant occurredAt,
    JsonNode data
) {
}