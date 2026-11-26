CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- -----------------------------------------------------
-- Purchase references (local projection)
-- -----------------------------------------------------

CREATE TABLE purchase_references (
                                     purchase_id UUID PRIMARY KEY,
                                     user_id VARCHAR(255) NOT NULL,
                                     item_name VARCHAR(255) NOT NULL,
                                     purchase_date DATE,
                                     source_event_id UUID NOT NULL,
                                     received_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_purchase_references_user
    ON purchase_references(user_id);

-- -----------------------------------------------------
-- Inbox: idempotent event processing
-- -----------------------------------------------------

CREATE TABLE inbox_events (
                              event_id UUID PRIMARY KEY,
                              event_type VARCHAR(100) NOT NULL,
                              aggregate_id UUID NOT NULL,
                              consumer_name VARCHAR(150) NOT NULL,
                              processed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_inbox_events_processed_at
    ON inbox_events(processed_at);

-- -----------------------------------------------------
-- Warranties
-- -----------------------------------------------------

CREATE TABLE warranties (
                            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                            user_id VARCHAR(255) NOT NULL,
                            purchase_id UUID NOT NULL,

                            warranty_provider VARCHAR(255),
                            warranty_reference VARCHAR(150),

                            warranty_type VARCHAR(30) NOT NULL,

                            start_date DATE,
                            end_date DATE,

                            coverage_description TEXT,

                            status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

                            created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                            version BIGINT NOT NULL DEFAULT 0,

                            CONSTRAINT fk_warranty_purchase_reference
                                FOREIGN KEY (purchase_id)
                                    REFERENCES purchase_references(purchase_id),

                            CONSTRAINT chk_warranty_type
                                CHECK (
                                    warranty_type IN (
                                                      'MANUFACTURER',
                                                      'EXTENDED',
                                                      'SELLER',
                                                      'THIRD_PARTY',
                                                      'OTHER'
                                        )
                                    ),

                            CONSTRAINT chk_warranty_status
                                CHECK (
                                    status IN ('ACTIVE', 'EXPIRED', 'VOIDED')
                                    ),

                            CONSTRAINT chk_warranty_dates
                                CHECK (
                                    start_date IS NULL
                                        OR end_date IS NULL
                                        OR end_date >= start_date
                                    )
);

CREATE INDEX idx_warranties_user
    ON warranties(user_id);

CREATE INDEX idx_warranties_purchase
    ON warranties(purchase_id);

CREATE INDEX idx_warranties_end_date
    ON warranties(end_date)
    WHERE status = 'ACTIVE';
