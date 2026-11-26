CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE purchases (
   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

   user_id VARCHAR(255) NOT NULL,

   item_name VARCHAR(255) NOT NULL,
   brand VARCHAR(150),
   model VARCHAR(150),

   purchase_price NUMERIC(12, 2),
   currency VARCHAR(3) NOT NULL DEFAULT 'LKR',

   seller_name VARCHAR(255),
   purchase_date DATE NOT NULL,

   notes TEXT,

   status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

   created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
   updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

   version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_purchases_user_id
    ON purchases(user_id);

CREATE INDEX idx_purchases_user_purchase_date
    ON purchases(user_id, purchase_date DESC);

CREATE TABLE outbox_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id UUID NOT NULL,

    event_type VARCHAR(150) NOT NULL,
    event_version INTEGER NOT NULL DEFAULT 1,

    payload JSONB NOT NULL,

    occurred_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    published_at TIMESTAMPTZ,

    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',

    attempts INTEGER NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    last_error TEXT,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_outbox_pending
    ON outbox_events(next_attempt_at, created_at)
    WHERE status = 'PENDING';

CREATE INDEX idx_outbox_aggregate
    ON outbox_events(aggregate_type, aggregate_id);