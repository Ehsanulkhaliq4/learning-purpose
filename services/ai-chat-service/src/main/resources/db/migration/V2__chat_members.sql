CREATE TABLE chat_members (
    id                    UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    conversation_id       UUID   NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    user_id               BIGINT NOT NULL,
    role                  VARCHAR(20) NOT NULL DEFAULT 'member',
    status                VARCHAR(20) NOT NULL DEFAULT 'pending',
    joined_at             TIMESTAMPTZ,
    left_at               TIMESTAMPTZ,
    invited_by            BIGINT,
    last_read_message_id  UUID,
    last_read_at          TIMESTAMPTZ,
    unread_count          INT NOT NULL DEFAULT 0,

    CONSTRAINT uq_chat_member UNIQUE (conversation_id, user_id),
    CONSTRAINT ck_member_role   CHECK (role   IN ('owner','admin','member')),
    CONSTRAINT ck_member_status CHECK (status IN ('pending','active','left','banned')),
    CONSTRAINT ck_active_joined CHECK (status != 'active' OR joined_at IS NOT NULL)
);

CREATE INDEX idx_members_user_active
    ON chat_members(user_id) WHERE status = 'active';
CREATE INDEX idx_members_pending
    ON chat_members(conversation_id) WHERE status = 'pending';