package ttrpg.CharManagementService.domain.characterdata;

import java.time.Instant;

import tools.jackson.databind.JsonNode;

import ttrpg.CharManagementService.domain.character.CharacterId;

public record CharacterDataSnapshot(
    CharacterDataId id,
    CharacterId characterId,
    JsonNode data,
    int version,
    Instant updatedAt
) {}
