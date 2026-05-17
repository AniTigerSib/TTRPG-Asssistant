package ttrpg.CharManagementService.presentation.dto;

import java.time.Instant;
import java.util.UUID;

public record CharacterResponse(
    UUID id,
    UUID ownerId,
    UUID campaignId,
    UUID gameSystemId,
    UUID templateId,
    String name,
    String avatarUrl,
    String status,
    Object data,
    int dataVersion,
    Instant createdAt,
    Instant updatedAt,
    Instant dataUpdatedAt
) {}
