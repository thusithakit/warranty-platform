
CREATE TABLE warranty_outbox_events (
                                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                                        aggregate_type VARCHAR(100) NOT NULL,
                                        aggregate_id UUID NOT NULL,

                                        event_type VARCHAR(150) NOT NULL,
                                        event_version INTEGER NOT NULL DEFAULT 1,

                                        payload JSONB NOT NULL,
                                        occurred_at TIMESTAMPTZ NOT NULL,

                                        published_at TIMESTAMPTZ,

                                        status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
                                        attempts INTEGER NOT NULL DEFAULT 0,
                                        next_attempt_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                        last_error TEXT,

                                        created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                        locked_at TIMESTAMPTZ,

                                        CONSTRAINT chk_warranty_outbox_status
                                            CHECK (status IN ('PENDING', 'PROCESSING', 'PUBLISHED', 'FAILED')),

                                        CONSTRAINT chk_warranty_outbox_attempts
                                            CHECK (attempts >= 0)
);

CREATE INDEX idx_warranty_outbox_pending
    ON warranty_outbox_events (next_attempt_at, created_at)
    WHERE status = 'PENDING';

CREATE INDEX idx_warranty_outbox_processing
    ON warranty_outbox_events (locked_at)
    WHERE status = 'PROCESSING';

CREATE INDEX idx_warranty_outbox_aggregate
    ON warranty_outbox_events (aggregate_type, aggregate_id);

CREATE INDEX idx_warranty_outbox_published
    ON warranty_outbox_events (published_at)
    WHERE status = 'PUBLISHED';
