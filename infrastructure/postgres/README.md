# PostgreSQL

Each service has an independent PostgreSQL container, database, application username, password and volume in the root Compose file.

The initial `POSTGRES_USER` is the database bootstrap/owner user for that container. The application must not use that owner identity in production.

For each service, the next service implementation phase must add Flyway migrations that:
1. Create/own schemas and tables using a migration/owner role.
2. Grant only required DML permissions to the runtime role.
3. Create outbox/inbox tables owned by the service.
4. Keep runtime credentials separate from migration credentials.

For stronger separation, introduce a one-time initialization script per database that creates `*_owner`, `*_migrator`, and `*_app` roles. Do not give the runtime role `CREATEDB`, `CREATEROLE`, or superuser privileges.
