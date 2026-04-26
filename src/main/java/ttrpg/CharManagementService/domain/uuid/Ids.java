package ttrpg.CharManagementService.domain.uuid;

import java.util.UUID;

public final class Ids {
    private Ids() {}

    public static void requireNonNull(UUID value, String type) {
        if (value == null) {
            throw new IllegalArgumentException(type + " value cannot be null");
        }
    }
}
