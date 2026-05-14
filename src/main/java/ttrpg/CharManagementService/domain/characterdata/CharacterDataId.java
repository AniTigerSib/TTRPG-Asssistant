package ttrpg.CharManagementService.domain.characterdata;

import java.util.UUID;

import ttrpg.CharManagementService.domain.exception.InvalidInputException;
import ttrpg.CharManagementService.domain.uuid.EntityId;
import ttrpg.CharManagementService.domain.uuid.Ids;

public record CharacterDataId(UUID value) implements EntityId {
    public CharacterDataId {
        Ids.requireNonNull(value, "CharacterDataId");
    }

    public static CharacterDataId newId() {
        return new CharacterDataId(UUID.randomUUID());
    }

    public static CharacterDataId fromString(String raw) {
        try {
            return new CharacterDataId(UUID.fromString(raw));
        } catch (IllegalArgumentException exception) {
            throw InvalidInputException.invalidValue("characterDataId", "characterDataId must be a valid UUID");
        }
    }
}
