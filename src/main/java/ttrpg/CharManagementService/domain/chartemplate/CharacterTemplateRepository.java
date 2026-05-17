package ttrpg.CharManagementService.domain.chartemplate;

import java.util.List;
import java.util.Optional;

import ttrpg.CharManagementService.domain.gamesystem.GameSystemId;

public interface CharacterTemplateRepository {

    List<CharacterTemplate> findByGameSystemId(GameSystemId gameSystemId);

    Optional<CharacterTemplate> findById(CharacterTemplateId id);

    CharacterTemplate save(CharacterTemplate template);
}
