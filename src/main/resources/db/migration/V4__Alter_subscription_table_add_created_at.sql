ALTER TABLE subscription
ADD COLUMN created_at TIMESTAMP;

UPDATE subscription SET created_at = NOW() WHERE created_at IS NULL;

ALTER TABLE subscription
ALTER COLUMN created_at SET NOT NULL;
