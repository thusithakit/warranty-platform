# Shared contracts

- `proto/`: gRPC contracts. Treat these as public service interfaces.
- `events/`: Kafka event envelopes and JSON Schemas.

Compatibility guidelines:
- Never reuse a removed Protobuf field number; reserve removed field numbers and names.
- Prefer additive changes.
- Version event schemas and topics when making breaking changes.
- Do not share persistence entities or database models through this repository.
- Generate Java/Python code from Protobuf during each service build; do not hand-edit generated code.
