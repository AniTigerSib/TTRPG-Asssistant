package ttrpg.CharManagementService.application.character;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ttrpg.CharManagementService.application.gamesystem.GameSystemRulesEngineRegistry;
import ttrpg.CharManagementService.application.shared.CharacterAccessPolicy;
import ttrpg.CharManagementService.domain.character.CharacterId;
import ttrpg.CharManagementService.domain.character.CharacterRepository;
import ttrpg.CharManagementService.domain.character.CharacterStatus;
import ttrpg.CharManagementService.domain.characterdata.CharacterDataRepository;
import ttrpg.CharManagementService.domain.chartemplate.CharacterTemplateRepository;
import ttrpg.CharManagementService.domain.exception.CharacterNotFoundException;
import ttrpg.CharManagementService.domain.exception.CharacterTemplateNotFoundException;
import ttrpg.CharManagementService.domain.exception.GameSystemNotFoundException;
import ttrpg.CharManagementService.domain.gamesystem.GameSystemRepository;
import ttrpg.CharManagementService.domain.shared.Checkers;
import ttrpg.CharManagementService.domain.user.User;

@Service
@RequiredArgsConstructor
public class UpdateCharacterUseCase {

    private final CharacterRepository characterRepository;
    private final CharacterDataRepository characterDataRepository;
    private final CharacterTemplateRepository characterTemplateRepository;
    private final GameSystemRepository gameSystemRepository;
    private final GameSystemRulesEngineRegistry rulesEngineRegistry;
    private final CharacterAccessPolicy characterAccessPolicy;

    @Transactional
    public CharacterDetails execute(User currentUser, CharacterId characterId, UpdateCharacterCommand command) {
        Checkers.requireNonNull(currentUser, "currentUser");
        Checkers.requireNonNull(characterId, "characterId");
        Checkers.requireNonNull(command, "command");
        Checkers.requireNonNull(command.data(), "data");

        var character = characterRepository.findById(characterId)
            .orElseThrow(() -> new CharacterNotFoundException(characterId.value()));
        characterAccessPolicy.assertCanEdit(currentUser, character);

        var normalizedData = command.data().deepCopy();
        if (character.getTemplateId() != null) {
            var template = characterTemplateRepository.findById(character.getTemplateId())
                .orElseThrow(() -> new CharacterTemplateNotFoundException(character.getTemplateId().value()));
            var gameSystem = gameSystemRepository.findById(character.getGameSystemId())
                .orElseThrow(() -> new GameSystemNotFoundException(character.getGameSystemId().asString()));
            normalizedData = rulesEngineRegistry.resolve(gameSystem.getCode())
                .validateAndNormalize(command.data(), template.getSchema());
        }

        character.update(command.name(), command.avatarUrl(), CharacterStatus.fromDatabaseValue(command.status()));
        var savedCharacter = characterRepository.save(character);
        var characterData = characterDataRepository.findByCharacterId(characterId)
            .orElseThrow(() -> new CharacterNotFoundException(characterId.value()));
        characterData.replace(normalizedData);
        var savedData = characterDataRepository.save(characterData);
        return new CharacterDetails(savedCharacter, savedData);
    }
}
