package ttrpg.CharManagementService.application.gamesystem;

import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;

public record ValidateCharacterTemplateCommand(
    UUID templateId,
    JsonNode characterData
) {}
