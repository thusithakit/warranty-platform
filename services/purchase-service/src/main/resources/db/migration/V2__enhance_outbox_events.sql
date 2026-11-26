ALTER TABLE outbox_events
    ADD COLUMN locked_at TIMESTAMPTZ;

CREATE INDEX idx_outbox_status_next_attempt
    ON outbox_events(status, next_attempt_at, created_at);

CREATE INDEX idx_outbox_locked_at
    ON outbox_events(locked_at)
    WHERE status = 'PROCESSING';
