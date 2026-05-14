package ttrpg.CharManagementService.application.character;

import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;

public record CreateCharacterCommand(
    UUID campaignId,
    String gameSystemCode,
    UUID templateId,
    String name,
    String avatarUrl,
    String status,
    JsonNode data
) {}
