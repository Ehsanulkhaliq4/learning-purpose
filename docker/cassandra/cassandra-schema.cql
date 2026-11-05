-- ============================================================
-- KEYSPACE: logs  (only logs)
-- ============================================================
CREATE KEYSPACE IF NOT EXISTS logs 
WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

USE logs;

CREATE TABLE IF NOT EXISTS application_logs (
    log_date date,
    log_time timestamp,
    log_id timeuuid,
    service_name text,
    log_level text,
    message text,
    container_id text,
    PRIMARY KEY ((log_date), log_time, log_id)
) WITH CLUSTERING ORDER BY (log_time DESC)
  AND default_time_to_live = 2592000;   -- 30 days


-- ============================================================
-- KEYSPACE: chat  (chat messages)
-- ============================================================

CREATE KEYSPACE IF NOT EXISTS chat
WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

USE chat;

CREATE TABLE IF NOT EXISTS messages_by_conversation (
    conversation_id uuid,
    message_id      timeuuid,
    sender_id       bigint,
    type            text,
    content         text,
    reply_to_id     timeuuid,
    created_at      timestamp,
    metadata        text,
    PRIMARY KEY ((conversation_id), message_id)
) WITH CLUSTERING ORDER BY (message_id DESC)
  AND compaction = {
      'class': 'TimeWindowCompactionStrategy',
      'compaction_window_unit': 'DAYS',
      'compaction_window_size': 30
  };

  CREATE TABLE IF NOT EXISTS message_receipts_by_message (
    message_id      timeuuid,
    user_id         bigint,
    conversation_id uuid,
    delivered_at    timestamp,
    read_at         timestamp,
    PRIMARY KEY ((message_id), user_id)
) WITH compaction = {'class': 'LeveledCompactionStrategy'};