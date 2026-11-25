# Keycloak local realm

The Compose stack imports `warranty-platform` realm from `realm-import/`.

## Included
- Realm: `warranty-platform`
- Realm roles: `USER`, `SUPPORT`, `ADMIN`
- Public OIDC client: `warranty-web`
- Authorization Code flow with PKCE (S256)

No demo user or client secret is included. Create local users in the Keycloak admin console or enable controlled bootstrap provisioning separately.

## Local URLs
- Admin console: http://localhost:8081/admin/
- Issuer: http://localhost:8081/realms/warranty-platform
- OIDC discovery: http://localhost:8081/realms/warranty-platform/.well-known/openid-configuration

## Backend service clients
Create a separate confidential client per service, enable service accounts, and assign only minimum required roles/scopes:
`api-gateway`, `user-service`, `purchase-service`, `warranty-service`, `document-service`, `notification-service`, `analytics-service`, `ai-service`.

Use client credentials for machine identity. User identity / actor context must be propagated separately and validated; never trust a user ID supplied only in an arbitrary request header.

## Production warning
`start-dev` is strictly for local development. Production must use Keycloak `start`, TLS, hostname configuration, secure cookies, trusted proxy settings, strong secrets, backup/restore, and a hardened database.
