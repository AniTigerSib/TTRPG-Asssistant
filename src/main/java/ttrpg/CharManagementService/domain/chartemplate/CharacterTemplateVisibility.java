package ttrpg.CharManagementService.domain.chartemplate;

public enum CharacterTemplateVisibility {
    VISIBLE,
    TESTING,
    DRAFT;

    public static CharacterTemplateVisibility fromDatabaseValue(String value) {
        return CharacterTemplateVisibility.valueOf(value.trim().toUpperCase());
    }

    public String toDatabaseValue() {
        return name().toLowerCase();
    }
}
