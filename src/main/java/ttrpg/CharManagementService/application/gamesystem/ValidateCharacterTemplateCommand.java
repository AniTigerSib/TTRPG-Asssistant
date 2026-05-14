package ttrpg.CharManagementService.application.gamesystem;

import java.util.UUID;

import tools.jackson.databind.JsonNode;

public record ValidateCharacterTemplateCommand(
    UUID templateId,
    JsonNode characterData
) {}
