package ttrpg.CharManagementService.domain.characterdata;

import java.time.Instant;

import com.fasterxml.jackson.databind.JsonNode;

import ttrpg.CharManagementService.domain.character.CharacterId;
import ttrpg.CharManagementService.domain.exception.InvalidInputException;
import ttrpg.CharManagementService.domain.shared.Checkers;

public class CharacterData {
    private final CharacterDataId id;
    private final CharacterId characterId;
    private JsonNode data;
    private int version;
    private Instant updatedAt;

    private CharacterData(
        CharacterDataId id,
        CharacterId characterId,
        JsonNode data,
        int version,
        Instant updatedAt
    ) {
        this.id = Checkers.requireNonNull(id, "id");
        this.characterId = Checkers.requireNonNull(characterId, "characterId");
        this.data = copyData(data);
        if (version < 1) {
            throw InvalidInputException.invalidValue("version", "version must be greater than 0");
        }
        this.version = version;
        this.updatedAt = Checkers.requireNonNull(updatedAt, "updatedAt");
    }

    public static CharacterData create(CharacterId characterId, JsonNode data) {
        return new CharacterData(CharacterDataId.newId(), characterId, data, 1, Instant.now());
    }

    public static CharacterData restore(CharacterDataSnapshot snapshot) {
        Checkers.requireNonNull(snapshot, "snapshot");
        return new CharacterData(
            snapshot.id(),
            snapshot.characterId(),
            snapshot.data(),
            snapshot.version(),
            snapshot.updatedAt()
        );
    }

    public CharacterDataId getId() { return id; }

    public CharacterId getCharacterId() { return characterId; }

    public JsonNode getData() { return copyData(data); }

    public int getVersion() { return version; }

    public Instant getUpdatedAt() { return updatedAt; }

    public void replace(JsonNode newData) {
        this.data = copyData(newData);
        this.version++;
        this.updatedAt = Instant.now();
    }

    public CharacterDataSnapshot snapshot() {
        return new CharacterDataSnapshot(id, characterId, copyData(data), version, updatedAt);
    }

    private static JsonNode copyData(JsonNode value) {
        return Checkers.requireNonNull(value, "data").deepCopy();
    }
}
