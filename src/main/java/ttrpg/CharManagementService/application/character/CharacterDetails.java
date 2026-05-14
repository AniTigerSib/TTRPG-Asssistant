package ttrpg.CharManagementService.application.character;

import ttrpg.CharManagementService.domain.character.Character;
import ttrpg.CharManagementService.domain.characterdata.CharacterData;

public record CharacterDetails(
    Character character,
    CharacterData characterData
) {}
