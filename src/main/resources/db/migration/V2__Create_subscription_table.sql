CREATE TABLE subscription (
    id BIGSERIAL PRIMARY KEY,
    event_id UUID NOT NULL,
    participant_email VARCHAR(255) NOT NULL,
    CONSTRAINT fk_subscription_event FOREIGN KEY (event_id) REFERENCES event(id) ON DELETE CASCADE
);
