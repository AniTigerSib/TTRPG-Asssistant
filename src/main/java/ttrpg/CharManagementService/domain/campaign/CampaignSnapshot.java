package ttrpg.CharManagementService.domain.campaign;

import java.time.Instant;

import ttrpg.CharManagementService.domain.gamesystem.GameSystemId;
import ttrpg.CharManagementService.domain.user.UserId;

public record CampaignSnapshot(
    CampaignId id,
    UserId ownerId,
    GameSystemId gameSystemId,
    String name,
    String description,
    CampaignVisibility visibility,
    Instant createdAt,
    Instant updatedAt
) {}
