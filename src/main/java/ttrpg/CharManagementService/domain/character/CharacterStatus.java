package ttrpg.CharManagementService.domain.character;

public enum CharacterStatus {
    DRAFT,
    ACTIVE,
    ARCHIVED;

    public static CharacterStatus fromDatabaseValue(String value) {
        return CharacterStatus.valueOf(value.trim().toUpperCase());
    }

    public String toDatabaseValue() {
        return name().toLowerCase();
    }
}
