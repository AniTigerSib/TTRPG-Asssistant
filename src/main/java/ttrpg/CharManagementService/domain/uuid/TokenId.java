package ttrpg.CharManagementService.domain.uuid;

import java.util.UUID;

public record TokenId(UUID value) implements EntityId {
    public TokenId {
        Ids.requireNonNull(value, "TokenId");
    }

    public static TokenId newId() {
        return new TokenId(UUID.randomUUID());
    }

    public static TokenId fromString(String raw) {
        return new TokenId(UUID.fromString(raw));
    }
}
