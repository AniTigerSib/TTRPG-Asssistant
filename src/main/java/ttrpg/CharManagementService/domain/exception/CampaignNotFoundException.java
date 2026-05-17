package ttrpg.CharManagementService.domain.exception;

import java.util.UUID;

public class CampaignNotFoundException extends ResourceNotFoundException {

    public CampaignNotFoundException(UUID campaignId) {
        super(ErrorCode.CAMPAIGN_NOT_FOUND, "Campaign not found: " + campaignId);
    }
}
