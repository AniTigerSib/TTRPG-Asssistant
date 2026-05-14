-- Single source of truth for character template visibility:
-- visibility is introduced only in this migration (V20260514104000).
ALTER TABLE character_templates
ADD COLUMN IF NOT EXISTS visibility VARCHAR(255) NOT NULL DEFAULT 'visible';

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_character_templates_visibility'
    ) THEN
        ALTER TABLE character_templates
            ADD CONSTRAINT chk_character_templates_visibility
                CHECK (visibility IN ('visible', 'testing', 'draft'));
    END IF;
END $$;
