CREATE TABLE campaigns (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL REFERENCES users(id),
    game_system_id UUID NOT NULL REFERENCES game_systems(id),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    visibility VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_campaigns_visibility
        CHECK (visibility IN ('private', 'invite_only', 'public'))
);

CREATE TABLE campaign_members (
    campaign_id UUID NOT NULL REFERENCES campaigns(id),
    user_id UUID NOT NULL REFERENCES users(id),
    role VARCHAR(255) NOT NULL,
    CONSTRAINT chk_campaign_members_role
        CHECK (role IN ('GM', 'PLAYER')),
    PRIMARY KEY (campaign_id, user_id)
);
