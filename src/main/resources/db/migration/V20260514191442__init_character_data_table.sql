CREATE TABLE character_data (
    id UUID PRIMARY KEY,
    character_id UUID NOT NULL REFERENCES characters(id),
    data JSONB NOT NULL,
    version INT NOT NULL DEFAULT 1,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
