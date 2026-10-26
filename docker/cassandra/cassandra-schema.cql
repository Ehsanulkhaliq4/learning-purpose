CREATE KEYSPACE IF NOT EXISTS logs 
WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

USE logs;

CREATE TABLE IF NOT EXISTS application_logs (
    log_date date,
    log_time timestamp,
    service_name text,
    log_level text,
    message text,
    container_id text,
    PRIMARY KEY ((log_date), log_time)
) WITH CLUSTERING ORDER BY (log_time DESC)
  AND default_time_to_live = 2592000;   -- 30 days