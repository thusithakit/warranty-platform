CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Records consumed Kafka events.
CREATE TABLE inbox_events (
  event_id UUID PRIMARY KEY,
  event_type VARCHAR(150) NOT NULL,
  aggregate_id UUID NOT NULL,
  consumer_name VARCHAR(150) NOT NULL,
  processed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notification_inbox_processed_at
    ON inbox_events(processed_at);


-- Persistent in-app notifications.
CREATE TABLE notifications (
   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

   user_id VARCHAR(255) NOT NULL,

   source_event_id UUID NOT NULL UNIQUE,

   notification_type VARCHAR(100) NOT NULL,

   title VARCHAR(255) NOT NULL,
   message TEXT NOT NULL,

   resource_type VARCHAR(100),
   resource_id UUID,

   status VARCHAR(30) NOT NULL DEFAULT 'UNREAD',

   created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
   read_at TIMESTAMPTZ,

   CONSTRAINT chk_notification_status
       CHECK (status IN ('UNREAD', 'READ', 'ARCHIVED'))
);

CREATE INDEX idx_notifications_user_created
    ON notifications(user_id, created_at DESC);

CREATE INDEX idx_notifications_user_unread
    ON notifications(user_id, created_at DESC)
    WHERE status = 'UNREAD';

CREATE INDEX idx_notifications_resource
    ON notifications(resource_type, resource_id);