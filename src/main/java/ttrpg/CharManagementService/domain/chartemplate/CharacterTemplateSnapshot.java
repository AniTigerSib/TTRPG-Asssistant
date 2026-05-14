package ttrpg.CharManagementService.domain.chartemplate;

import java.time.Instant;

import tools.jackson.databind.JsonNode;

import ttrpg.CharManagementService.domain.gamesystem.GameSystemId;

public record CharacterTemplateSnapshot(
    CharacterTemplateId id,
    GameSystemId gameSystemId,
    String name,
    JsonNode schema,
    int version,
    boolean official,
    CharacterTemplateVisibility visibility,
    Instant createdAt
) {}
