-- Keep the existing PostgreSQL database compatible with the video pipeline.
-- ALTER TABLE ... IF EXISTS is a no-op on a fresh database; Hibernate creates
-- the tables afterward.
ALTER TABLE IF EXISTS stories
    ADD COLUMN IF NOT EXISTS asset_status VARCHAR(255) NOT NULL DEFAULT 'PENDING';

ALTER TABLE IF EXISTS stories
    ADD COLUMN IF NOT EXISTS tts_voice VARCHAR(255);

ALTER TABLE IF EXISTS story_queue
    ADD COLUMN IF NOT EXISTS attempts INTEGER NOT NULL DEFAULT 0;

ALTER TABLE IF EXISTS story_queue
    ADD COLUMN IF NOT EXISTS error_message TEXT;
