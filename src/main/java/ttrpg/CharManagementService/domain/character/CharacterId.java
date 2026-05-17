package ttrpg.CharManagementService.domain.character;

import java.util.UUID;

import ttrpg.CharManagementService.domain.exception.InvalidInputException;
import ttrpg.CharManagementService.domain.uuid.EntityId;
import ttrpg.CharManagementService.domain.uuid.Ids;

public record CharacterId(UUID value) implements EntityId {
    public CharacterId {
        Ids.requireNonNull(value, "CharacterId");
    }

    public static CharacterId newId() {
        return new CharacterId(UUID.randomUUID());
    }

    public static CharacterId fromString(String raw) {
        try {
            return new CharacterId(UUID.fromString(raw));
        } catch (IllegalArgumentException exception) {
            throw InvalidInputException.invalidValue("characterId", "characterId must be a valid UUID");
        }
    }
}
