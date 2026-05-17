package ttrpg.CharManagementService.domain.uuid;

import java.util.UUID;

import ttrpg.CharManagementService.domain.exception.InvalidInputException;

public final class Ids {
    private Ids() {}

    public static void requireNonNull(UUID value, String type) {
        if (value == null) {
            throw InvalidInputException.nullField(type);
        }
    }
}
