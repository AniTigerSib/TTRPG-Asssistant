package ttrpg.CharManagementService.domain.user;

import java.util.UUID;

import ttrpg.CharManagementService.domain.exception.InvalidInputException;
import ttrpg.CharManagementService.domain.uuid.EntityId;
import ttrpg.CharManagementService.domain.uuid.Ids;

public record UserId(UUID value) implements EntityId {
    public UserId {
        Ids.requireNonNull(value, "UserId");
    }

    public static UserId newId() {
        return new UserId(UUID.randomUUID());
    }

    public static UserId fromString(String raw) {
        try {
            return new UserId(UUID.fromString(raw));
        } catch (IllegalArgumentException exception) {
            throw InvalidInputException.invalidValue("userId", "userId must be a valid UUID");
        }
    }
}
