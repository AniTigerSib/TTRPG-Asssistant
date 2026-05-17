CREATE TABLE characters (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL REFERENCES users(id),
    campaign_id UUID REFERENCES campaigns(id),
    game_system_id UUID NOT NULL REFERENCES game_systems(id),
    template_id UUID REFERENCES character_templates(id),
    name VARCHAR(255) NOT NULL,
    avatar_url TEXT,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_characters_status
        CHECK (status IN ('draft', 'active', 'archived'))
);
