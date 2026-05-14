package ttrpg.CharManagementService.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import ttrpg.CharManagementService.domain.campaign.Campaign;
import ttrpg.CharManagementService.domain.campaign.CampaignId;
import ttrpg.CharManagementService.domain.campaign.CampaignSnapshot;
import ttrpg.CharManagementService.domain.campaign.CampaignVisibility;
import ttrpg.CharManagementService.domain.gamesystem.GameSystemId;
import ttrpg.CharManagementService.domain.user.UserId;
import ttrpg.CharManagementService.infrastructure.persistence.entity.CampaignJpaEntity;

@Component
public class CampaignPersistenceMapper {

    public Campaign toDomain(CampaignJpaEntity entity) {
        return Campaign.restore(
            new CampaignSnapshot(
                new CampaignId(entity.getId()),
                new UserId(entity.getOwnerId()),
                new GameSystemId(entity.getGameSystemId()),
                entity.getName(),
                entity.getDescription(),
                CampaignVisibility.fromDatabaseValue(entity.getVisibility()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
            )
        );
    }
}
