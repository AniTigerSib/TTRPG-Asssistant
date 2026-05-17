package ttrpg.CharManagementService.domain.gamesystem;

import java.util.UUID;

import ttrpg.CharManagementService.domain.exception.InvalidInputException;
import ttrpg.CharManagementService.domain.uuid.EntityId;
import ttrpg.CharManagementService.domain.uuid.Ids;

public record GameSystemId(UUID value) implements EntityId {
    public GameSystemId {
        Ids.requireNonNull(value, "GameSystemId");
    }

    public static GameSystemId newId() {
        return new GameSystemId(UUID.randomUUID());
    }

    public static GameSystemId fromString(String raw) {
        try {
            return new GameSystemId(UUID.fromString(raw));
        } catch (IllegalArgumentException exception) {
            throw InvalidInputException.invalidValue("gameSystemId", "gameSystemId must be a valid UUID");
        }
    }
}
