package ttrpg.CharManagementService.domain.uuid;

import java.util.UUID;

import ttrpg.CharManagementService.domain.exception.InvalidInputException;

public record TokenId(UUID value) implements EntityId {
    public TokenId {
        Ids.requireNonNull(value, "TokenId");
    }

    public static TokenId newId() {
        return new TokenId(UUID.randomUUID());
    }

    public static TokenId fromString(String raw) {
        try {
            return new TokenId(UUID.fromString(raw));
        } catch (IllegalArgumentException exception) {
            throw InvalidInputException.invalidValue("tokenId", "tokenId must be a valid UUID");
        }
    }
}
