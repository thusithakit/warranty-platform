CREATE TABLE warranty_reminder_dispatches (
                                              id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                                              warranty_id UUID NOT NULL,
                                              user_id VARCHAR(255) NOT NULL,
                                              purchase_id UUID NOT NULL,

                                              reminder_days_before INTEGER NOT NULL,

                                              scheduled_for DATE NOT NULL,
                                              dispatched_at TIMESTAMPTZ,

                                              status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

                                              created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                              CONSTRAINT uq_warranty_reminder
                                                  UNIQUE (warranty_id, reminder_days_before),

                                              CONSTRAINT chk_reminder_days_before
                                                  CHECK (reminder_days_before BETWEEN 1 AND 365),

                                              CONSTRAINT chk_reminder_dispatch_status
                                                  CHECK (status IN ('PENDING', 'DISPATCHED', 'CANCELLED'))
);

CREATE INDEX idx_warranty_reminders_pending
    ON warranty_reminder_dispatches(scheduled_for)
    WHERE status = 'PENDING';

CREATE INDEX idx_warranty_reminders_user
    ON warranty_reminder_dispatches(user_id, scheduled_for DESC);

CREATE INDEX idx_warranty_reminders_warranty
    ON warranty_reminder_dispatches(warranty_id);