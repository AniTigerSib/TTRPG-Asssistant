package ttrpg.CharManagementService.application.character;

import com.fasterxml.jackson.databind.JsonNode;

public record UpdateCharacterCommand(
    String name,
    String avatarUrl,
    String status,
    JsonNode data
) {}
