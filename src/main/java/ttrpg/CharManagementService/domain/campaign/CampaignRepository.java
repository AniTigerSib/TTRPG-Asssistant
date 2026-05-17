package ttrpg.CharManagementService.domain.campaign;

import java.util.Optional;

public interface CampaignRepository {

    Optional<Campaign> findById(CampaignId id);
}
