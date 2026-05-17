package ttrpg.CharManagementService.domain.auth.RefreshToken;

import java.util.UUID;

import ttrpg.CharManagementService.domain.exception.InvalidInputException;
import ttrpg.CharManagementService.domain.uuid.EntityId;
import ttrpg.CharManagementService.domain.uuid.Ids;

public record RefreshTokenId(UUID value) implements EntityId {
    public RefreshTokenId {
        Ids.requireNonNull(value, "RefreshTokenId");
    }

    public static RefreshTokenId newId() {
        return new RefreshTokenId(UUID.randomUUID());
    }

    public static RefreshTokenId fromString(String raw) {
        try {
            return new RefreshTokenId(UUID.fromString(raw));
        } catch (IllegalArgumentException exception) {
            throw InvalidInputException.invalidValue("refreshTokenId", "refreshTokenId must be a valid UUID");
        }
    }
}
