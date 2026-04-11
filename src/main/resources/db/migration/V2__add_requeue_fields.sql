ALTER TABLE queue_entries
    ADD COLUMN requeue_count INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN sort_key      BIGINT;

UPDATE queue_entries SET sort_key = token_number * 1000;

ALTER TABLE queue_entries
    ALTER COLUMN sort_key SET NOT NULL,
    ALTER COLUMN sort_key SET DEFAULT 0;
