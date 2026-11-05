CREATE TABLE dm_pairs (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_a_id       BIGINT NOT NULL,
    user_b_id       BIGINT NOT NULL,
    conversation_id UUID   NOT NULL UNIQUE REFERENCES conversations(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT ck_dm_order CHECK (user_a_id < user_b_id),
    CONSTRAINT uq_dm_pair  UNIQUE (user_a_id, user_b_id)
);