package ttrpg.CharManagementService.domain.character;

import java.time.Instant;

import ttrpg.CharManagementService.domain.campaign.CampaignId;
import ttrpg.CharManagementService.domain.chartemplate.CharacterTemplateId;
import ttrpg.CharManagementService.domain.gamesystem.GameSystemId;
import ttrpg.CharManagementService.domain.user.UserId;

public record CharacterSnapshot(
    CharacterId id,
    UserId ownerId,
    CampaignId campaignId,
    GameSystemId gameSystemId,
    CharacterTemplateId templateId,
    String name,
    String avatarUrl,
    CharacterStatus status,
    Instant createdAt,
    Instant updatedAt
) {}
