package ttrpg.CharManagementService.domain.chartemplate;

import java.time.Instant;

import tools.jackson.databind.JsonNode;

import ttrpg.CharManagementService.domain.gamesystem.GameSystemId;
import ttrpg.CharManagementService.domain.shared.Checkers;

public class CharacterTemplate {
    private final CharacterTemplateId id;
    private final GameSystemId gameSystemId;
    private final String name;
    private final JsonNode schema;
    private final int version;
    private final boolean official;
    private final CharacterTemplateVisibility visibility;
    private final Instant createdAt;

    private CharacterTemplate(
        CharacterTemplateId id,
        GameSystemId gameSystemId,
        String name,
        JsonNode schema,
        int version,
        boolean official,
        CharacterTemplateVisibility visibility,
        Instant createdAt
    ) {
        this.id = Checkers.requireNonNull(id, "id");
        this.gameSystemId = Checkers.requireNonNull(gameSystemId, "gameSystemId");
        this.name = Checkers.requireStringNonBlank(name, "name");
        this.schema = copySchema(schema);
        if (version < 1) {
            throw ttrpg.CharManagementService.domain.exception.InvalidInputException.invalidValue(
                "version",
                "version must be greater than 0"
            );
        }
        this.version = version;
        this.official = official;
        this.visibility = Checkers.requireNonNull(visibility, "visibility");
        this.createdAt = Checkers.requireNonNull(createdAt, "createdAt");
    }

    public static CharacterTemplate create(
        GameSystemId gameSystemId,
        String name,
        JsonNode schema,
        int version,
        boolean official,
        CharacterTemplateVisibility visibility
    ) {
        return new CharacterTemplate(
            CharacterTemplateId.newId(),
            gameSystemId,
            name,
            schema,
            version,
            official,
            visibility,
            Instant.now()
        );
    }

    public static CharacterTemplate restore(CharacterTemplateSnapshot snapshot) {
        Checkers.requireNonNull(snapshot, "snapshot");
        return new CharacterTemplate(
            snapshot.id(),
            snapshot.gameSystemId(),
            snapshot.name(),
            snapshot.schema(),
            snapshot.version(),
            snapshot.official(),
            snapshot.visibility(),
            snapshot.createdAt()
        );
    }

    public CharacterTemplateId getId() { return id; }

    public GameSystemId getGameSystemId() { return gameSystemId; }

    public String getName() { return name; }

    public JsonNode getSchema() { return copySchema(schema); }

    public int getVersion() { return version; }

    public boolean isOfficial() { return official; }

    public CharacterTemplateVisibility getVisibility() { return visibility; }

    public Instant getCreatedAt() { return createdAt; }

    public CharacterTemplateSnapshot snapshot() {
        return new CharacterTemplateSnapshot(
            id,
            gameSystemId,
            name,
            copySchema(schema),
            version,
            official,
            visibility,
            createdAt
        );
    }

    private static JsonNode copySchema(JsonNode schema) {
        return Checkers.requireNonNull(schema, "schema").deepCopy();
    }
}
