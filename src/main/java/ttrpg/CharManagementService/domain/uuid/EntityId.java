package ttrpg.CharManagementService.domain.uuid;

import java.util.UUID;

public interface EntityId {
    UUID value();
    
    default String asString() {
        return value().toString();
    }
}
