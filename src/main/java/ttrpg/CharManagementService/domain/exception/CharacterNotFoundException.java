package ttrpg.CharManagementService.domain.exception;

import java.util.UUID;

public class CharacterNotFoundException extends ResourceNotFoundException {

    public CharacterNotFoundException(UUID characterId) {
        super(ErrorCode.CHARACTER_NOT_FOUND, "Character not found: " + characterId);
    }
}
