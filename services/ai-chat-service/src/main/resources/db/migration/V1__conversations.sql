CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE conversations (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    type            VARCHAR(20)  NOT NULL,
    name            VARCHAR(255),
    description     TEXT,
    avatar_url      VARCHAR(500),
    visibility      VARCHAR(20),
    invite_code     VARCHAR(50) UNIQUE,
    workspace_id    UUID,
    created_by      BIGINT       NOT NULL,
    last_message_at TIMESTAMPTZ,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    archived_at     TIMESTAMPTZ,

    CONSTRAINT ck_conv_type
        CHECK (type IN ('dm','group','channel')),
    CONSTRAINT ck_conv_visibility
        CHECK (visibility IS NULL OR visibility IN ('public','private')),
    CONSTRAINT ck_conv_group_fields
        CHECK (type != 'group' OR (name IS NOT NULL AND visibility IS NOT NULL))
);

CREATE INDEX idx_conv_last_message
    ON conversations(last_message_at DESC NULLS LAST);