package ttrpg.CharManagementService.domain.characterdata;

import java.util.Optional;

import ttrpg.CharManagementService.domain.character.CharacterId;

public interface CharacterDataRepository {

    CharacterData save(CharacterData characterData);

    Optional<CharacterData> findByCharacterId(CharacterId characterId);
}
