# Warranty Platform

Docker-first microservices foundation for a digital warranty and purchase-document platform.

## Architecture decisions

- Java 25 + Spring Boot 4.x
- Spring Cloud Gateway (WebFlux)
- Apache Kafka 4.x in KRaft mode
- PostgreSQL: one container/database/user per service
- Keycloak for authentication and realm/client roles
- gRPC + Protocol Buffers for synchronous service communication
- Kafka + transactional outbox/inbox for asynchronous communication
- Python FastAPI + OCR + Groq for AI document processing (added in a later phase)
- Cloudinary for document storage (added in the Document Service phase)

## Repository layout

```text
gateway/api-gateway/     Spring Cloud Gateway starter
contracts/proto/         Shared gRPC contracts
contracts/events/        Versioned Kafka event schemas
infrastructure/          Keycloak, PostgreSQL and Kafka configuration
docker-compose.yml       Local infrastructure and gateway
.env.example             Local development configuration template
```

## Prerequisites

- Docker Engine/Desktop with Compose v2
- Git
- Java 25 and Maven 3.9+ (only needed to run/build the gateway outside Docker)

## First-time setup

1. Copy `.env.example` to `.env`.
2. Replace the development passwords/secrets if desired.
3. Start the infrastructure:

   ```bash
   docker compose up -d --build
   ```

4. Check container health:

   ```bash
   docker compose ps
   ```

5. Open:
   - Gateway health: http://localhost:8080/actuator/health
   - Keycloak: http://localhost:8081
   - Kafka UI: http://localhost:8090

Keycloak admin credentials and database credentials are supplied through `.env`.

## Stop / reset

Stop containers but retain data:

```bash
docker compose down
```

Destroy local data volumes (DESTRUCTIVE):

```bash
docker compose down -v
```

## PostgreSQL databases

Each service has its own PostgreSQL container, database, application user and persistent volume:

| Service | Host port | Database |
|---|---:|---|
| Keycloak | 5540 | keycloak_db |
| User | 5541 | user_db |
| Purchase | 5542 | purchase_db |
| Warranty | 5543 | warranty_db |
| Document | 5544 | document_db |
| Notification | 5545 | notification_db |
| Analytics | 5546 | analytics_db |
| AI | 5547 | ai_db |

Ports are published for local development only. In production, do not publish database ports and use private networking / managed secrets.

## Keycloak

The `warranty-platform` realm is imported at startup. It contains:
- Realm roles: `USER`, `SUPPORT`, `ADMIN`
- Public OIDC client: `warranty-web` (authorization code + PKCE)
- Development user: configured by environment variables in the Keycloak container

The realm import contains no demo users or client secrets. Create local users in the Keycloak console and confidential service clients with service accounts before enabling service-to-service authentication. See `infrastructure/keycloak/README.md`.

## Kafka

Kafka runs in single-node KRaft mode for local development. It is not a production HA topology.

The broker is available to containers at `kafka:9092` and to host tools at `localhost:9094`.

Topic creation and event schemas are documented in `contracts/events/README.md`.

## Important architecture rules

1. Each service owns its database and migrations.
2. No cross-service foreign keys or direct database access.
3. Each service's runtime database user must not be a superuser or schema owner.
4. Use gRPC for request/response service communication.
5. Use Kafka for domain events and asynchronous workflows.
6. Publish events through a transactional outbox; consume idempotently with an inbox.
7. Kafka delivery is at-least-once. Design consumers for effectively-once business effects.
8. Gateway authentication is not the only security boundary; services must validate tokens and enforce authorization and resource ownership.
9. Do not put secrets in Git. `.env` is ignored.

## Phase 1 scope

This repository is an infrastructure and contract foundation. Domain services, outbox publishers, Kafka consumers, gRPC server implementations, Cloudinary integration and Python AI processing are intentionally delivered in subsequent phases.
