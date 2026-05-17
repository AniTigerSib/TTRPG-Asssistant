package ttrpg.CharManagementService.domain.chartemplate;

import java.util.UUID;

import ttrpg.CharManagementService.domain.exception.InvalidInputException;
import ttrpg.CharManagementService.domain.uuid.EntityId;
import ttrpg.CharManagementService.domain.uuid.Ids;

public record CharacterTemplateId(UUID value) implements EntityId {
    public CharacterTemplateId {
        Ids.requireNonNull(value, "CharacterTemplateId");
    }

    public static CharacterTemplateId newId() {
        return new CharacterTemplateId(UUID.randomUUID());
    }

    public static CharacterTemplateId fromString(String raw) {
        try {
            return new CharacterTemplateId(UUID.fromString(raw));
        } catch (IllegalArgumentException exception) {
            throw InvalidInputException.invalidValue("templateId", "templateId must be a valid UUID");
        }
    }
}
