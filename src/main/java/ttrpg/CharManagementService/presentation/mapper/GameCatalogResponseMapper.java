package ttrpg.CharManagementService.presentation.mapper;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import ttrpg.CharManagementService.application.gamesystem.CharacterTemplateValidationResult;
import ttrpg.CharManagementService.domain.chartemplate.CharacterTemplate;
import ttrpg.CharManagementService.domain.gamesystem.GameSystem;
import ttrpg.CharManagementService.presentation.dto.CharacterTemplateResponse;
import ttrpg.CharManagementService.presentation.dto.GameSystemResponse;
import ttrpg.CharManagementService.presentation.dto.ValidateCharacterResponse;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class GameCatalogResponseMapper {

    private final ObjectMapper objectMapper;

    public GameSystemResponse toGameSystemResponse(GameSystem gameSystem) {
        return new GameSystemResponse(
            gameSystem.getId().value(),
            gameSystem.getCode(),
            gameSystem.getName(),
            gameSystem.getVersion(),
            gameSystem.getDescription(),
            gameSystem.getCreatedAt()
        );
    }

    public CharacterTemplateResponse toCharacterTemplateResponse(CharacterTemplate template) {
        return new CharacterTemplateResponse(
            template.getId().value(),
            template.getGameSystemId().value(),
            template.getName(),
            template.getVersion(),
            template.isOfficial(),
            template.getVisibility().toDatabaseValue(),
            objectMapper.convertValue(template.getSchema(), Object.class),
            template.getCreatedAt()
        );
    }

    public ValidateCharacterResponse toValidateCharacterResponse(CharacterTemplateValidationResult result) {
        return new ValidateCharacterResponse(
            result.template().getId().value(),
            result.gameSystem().getCode(),
            result.template().getName(),
            objectMapper.convertValue(result.normalizedCharacterData(), Object.class)
        );
    }
}
