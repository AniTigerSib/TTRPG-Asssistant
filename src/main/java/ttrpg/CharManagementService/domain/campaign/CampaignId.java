package ttrpg.CharManagementService.domain.campaign;

import java.util.UUID;

import ttrpg.CharManagementService.domain.exception.InvalidInputException;
import ttrpg.CharManagementService.domain.uuid.EntityId;
import ttrpg.CharManagementService.domain.uuid.Ids;

public record CampaignId(UUID value) implements EntityId {
    public CampaignId {
        Ids.requireNonNull(value, "CampaignId");
    }

    public static CampaignId fromString(String raw) {
        try {
            return new CampaignId(UUID.fromString(raw));
        } catch (IllegalArgumentException exception) {
            throw InvalidInputException.invalidValue("campaignId", "campaignId must be a valid UUID");
        }
    }
}
