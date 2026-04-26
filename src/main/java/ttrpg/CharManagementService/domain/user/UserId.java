package ttrpg.CharManagementService.domain.user;

import java.util.UUID;

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
        return new UserId(UUID.fromString(raw));
    }
}
