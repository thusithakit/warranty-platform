# Kafka event contracts

Events are versioned independently of service implementation. The initial contracts use JSON Schema Draft 2020-12.

## Naming

Use lowercase dotted topic names, for example:

- `purchase.created.v1`
- `document.uploaded.v1`
- `ai.extraction.completed.v1`
- `warranty.created.v1`

Use the aggregate ID as the Kafka message key to preserve per-aggregate ordering within a partition.

## Envelope

Every event should carry:
- `eventId`: globally unique UUID used for inbox deduplication
- `eventType`: stable event name
- `eventVersion`: integer schema version
- `occurredAt`: UTC ISO-8601 timestamp
- `producer`: service name
- `correlationId`: distributed trace/workflow correlation
- `aggregateId`: aggregate identifier
- `data`: event-specific payload

Do not include secrets, access tokens, raw documents, or unnecessary personal information.

## Delivery semantics

- Producers publish from an outbox.
- Consumers use an inbox / processed-event table.
- Acknowledge/commit offsets only after the local database transaction commits.
- Assume at-least-once delivery.
- Use retry topics and a dead-letter topic for poison messages.
- Do not claim end-to-end exactly-once business processing.

## Initial local topics

Create topics as services are introduced. Suggested starting topics:
- `purchase.created.v1`
- `purchase.updated.v1`
- `purchase.deleted.v1`
- `document.uploaded.v1`
- `ai.extraction.completed.v1`
- `ai.extraction.failed.v1`
- `warranty.created.v1`
- `warranty.updated.v1`
- `warranty.deleted.v1`
- `warranty.expiring.v1`
- `notification.sent.v1`
- `platform.dlq.v1`
