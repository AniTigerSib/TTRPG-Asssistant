ALTER TABLE character_templates
ADD COLUMN IF NOT EXISTS visibility VARCHAR(255) NOT NULL DEFAULT 'visible';

ALTER TABLE character_templates
ADD CONSTRAINT chk_character_templates_visibility
CHECK (visibility IN ('visible', 'testing', 'draft'));
