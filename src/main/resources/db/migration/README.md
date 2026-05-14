# Migration order notes

- `V20260512225401__init_character_templates_table.sql` creates the base `character_templates` table **without** `visibility`.
- `V20260514104000__add_visibility_to_character_templates.sql` is the single source of truth for introducing `visibility` and its check constraint.
- Follow-up migrations (for example seeds/updates) may rely on `visibility`, but must not re-introduce the column or duplicate the same constraint.

This prevents duplicate-column situations on mixed environments (clean database vs partially migrated database).
