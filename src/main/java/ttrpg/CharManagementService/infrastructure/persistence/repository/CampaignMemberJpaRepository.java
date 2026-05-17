package ttrpg.CharManagementService.infrastructure.persistence.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import ttrpg.CharManagementService.infrastructure.persistence.entity.CampaignMemberJpaEntity;
import ttrpg.CharManagementService.infrastructure.persistence.entity.CampaignMemberJpaEntity.CampaignMemberId;

public interface CampaignMemberJpaRepository extends JpaRepository<CampaignMemberJpaEntity, CampaignMemberId> {

    boolean existsByIdCampaignIdAndIdUserId(UUID campaignId, UUID userId);
}
