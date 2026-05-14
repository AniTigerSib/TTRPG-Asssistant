CREATE TABLE character_templates (
    id UUID PRIMARY KEY,
    game_system_id UUID NOT NULL REFERENCES game_systems(id),
    name VARCHAR(255) NOT NULL,
    schema JSONB NOT NULL,
    version INT NOT NULL,
    is_official BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
