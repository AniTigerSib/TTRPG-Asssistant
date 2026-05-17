package ttrpg.CharManagementService.domain.campaign;

import ttrpg.CharManagementService.domain.user.UserId;

public interface CampaignMemberRepository {

    boolean existsByCampaignIdAndUserId(CampaignId campaignId, UserId userId);
}
