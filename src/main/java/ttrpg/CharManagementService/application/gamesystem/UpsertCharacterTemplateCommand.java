package ttrpg.CharManagementService.application.gamesystem;

import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;

public record UpsertCharacterTemplateCommand(
    UUID gameSystemId,
    String name,
    JsonNode schema,
    int version,
    boolean official,
    String visibility
) {}
