CREATE TABLE invites (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    type            VARCHAR(20) NOT NULL,
    conversation_id UUID REFERENCES conversations(id) ON DELETE CASCADE,
    inviter_id      BIGINT      NOT NULL,
    invitee_id      BIGINT      NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'pending',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at      TIMESTAMPTZ,

    CONSTRAINT ck_invite_type   CHECK (type   IN ('group','workspace','team')),
    CONSTRAINT ck_invite_status CHECK (status IN ('pending','accepted','declined','expired'))
);

CREATE INDEX idx_invites_invitee_pending
    ON invites(invitee_id) WHERE status = 'pending';