package ttrpg.CharManagementService.application.gamesystem;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ttrpg.CharManagementService.domain.chartemplate.CharacterTemplateId;
import ttrpg.CharManagementService.domain.chartemplate.CharacterTemplateRepository;
import ttrpg.CharManagementService.domain.exception.CharacterTemplateNotFoundException;
import ttrpg.CharManagementService.domain.exception.GameSystemNotFoundException;
import ttrpg.CharManagementService.domain.gamesystem.GameSystemRepository;
import ttrpg.CharManagementService.domain.shared.Checkers;

@Service
@RequiredArgsConstructor
public class ValidateCharacterTemplateUseCase {

    private final CharacterTemplateRepository characterTemplateRepository;
    private final GameSystemRepository gameSystemRepository;
    private final GameSystemRulesEngineRegistry rulesEngineRegistry;

    @Transactional(readOnly = true)
    public CharacterTemplateValidationResult execute(ValidateCharacterTemplateCommand command) {
        Checkers.requireNonNull(command, "command");
        Checkers.requireNonNull(command.templateId(), "templateId");
        Checkers.requireNonNull(command.characterData(), "characterData");

        var templateId = new CharacterTemplateId(command.templateId());
        var template = characterTemplateRepository.findById(templateId)
            .orElseThrow(() -> new CharacterTemplateNotFoundException(command.templateId()));
        var gameSystem = gameSystemRepository.findById(template.getGameSystemId())
            .orElseThrow(() -> new GameSystemNotFoundException(template.getGameSystemId().asString()));

        var normalizedCharacterData = rulesEngineRegistry.resolve(gameSystem.getCode())
            .validateAndNormalize(command.characterData(), template.getSchema());

        return new CharacterTemplateValidationResult(gameSystem, template, normalizedCharacterData);
    }
}
