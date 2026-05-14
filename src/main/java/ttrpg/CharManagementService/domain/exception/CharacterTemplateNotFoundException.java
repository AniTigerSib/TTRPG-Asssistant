package ttrpg.CharManagementService.domain.exception;

import java.util.UUID;

public class CharacterTemplateNotFoundException extends ResourceNotFoundException {

    public CharacterTemplateNotFoundException(UUID templateId) {
        super(ErrorCode.CHARACTER_TEMPLATE_NOT_FOUND, "Character template not found: " + templateId);
    }
}
