package ttrpg.CharManagementService.application.gamesystem;

import tools.jackson.databind.JsonNode;

import ttrpg.CharManagementService.domain.chartemplate.CharacterTemplate;
import ttrpg.CharManagementService.domain.gamesystem.GameSystem;

public record CharacterTemplateValidationResult(
    GameSystem gameSystem,
    CharacterTemplate template,
    JsonNode normalizedCharacterData
) {}
