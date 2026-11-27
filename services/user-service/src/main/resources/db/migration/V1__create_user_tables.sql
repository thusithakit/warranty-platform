CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE user_profiles (
                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                               keycloak_user_id VARCHAR(255) NOT NULL UNIQUE,

                               email VARCHAR(320),
                               first_name VARCHAR(100),
                               last_name VARCHAR(100),
                               phone_number VARCHAR(30),
                               avatar_url TEXT,

                               created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                               version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE user_preferences (
                                  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                                  keycloak_user_id VARCHAR(255) NOT NULL UNIQUE,

                                  locale VARCHAR(20) NOT NULL DEFAULT 'en-LK',
                                  timezone VARCHAR(60) NOT NULL DEFAULT 'Asia/Colombo',
                                  currency VARCHAR(3) NOT NULL DEFAULT 'LKR',

                                  email_notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE,
                                  push_notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE,
                                  in_app_notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE,

                                  warranty_reminder_days INTEGER[] NOT NULL DEFAULT ARRAY[30, 7, 1],

                                  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                  CONSTRAINT fk_preferences_user
                                      FOREIGN KEY (keycloak_user_id)
                                          REFERENCES user_profiles(keycloak_user_id)
                                          ON DELETE CASCADE,

                                  CONSTRAINT chk_currency_format
                                      CHECK (currency ~ '^[A-Z]{3}$'),

    CONSTRAINT chk_reminder_days
        CHECK (
            cardinality(warranty_reminder_days) BETWEEN 1 AND 10
            AND 0 < ALL(warranty_reminder_days)
            AND 365 >= ALL(warranty_reminder_days)
        )
);

CREATE INDEX idx_user_profiles_email
    ON user_profiles(email);

CREATE INDEX idx_user_profiles_created_at
    ON user_profiles(created_at DESC);