CREATE TABLE event (
    id UUID PRIMARY KEY,
    max_participants INTEGER NOT NULL,
    registered_participants INTEGER NOT NULL,
    title VARCHAR(255) NOT NULL,
    date DATE NOT NULL,
    description TEXT
);
