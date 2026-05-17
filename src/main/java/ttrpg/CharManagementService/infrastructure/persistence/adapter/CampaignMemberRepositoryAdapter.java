package ttrpg.CharManagementService.infrastructure.persistence.adapter;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import ttrpg.CharManagementService.domain.campaign.CampaignId;
import ttrpg.CharManagementService.domain.campaign.CampaignMemberRepository;
import ttrpg.CharManagementService.domain.user.UserId;
import ttrpg.CharManagementService.infrastructure.persistence.repository.CampaignMemberJpaRepository;

@Repository
@RequiredArgsConstructor
public class CampaignMemberRepositoryAdapter implements CampaignMemberRepository {

    private final CampaignMemberJpaRepository jpaRepository;

    @Override
    public boolean existsByCampaignIdAndUserId(CampaignId campaignId, UserId userId) {
        return jpaRepository.existsByIdCampaignIdAndIdUserId(campaignId.value(), userId.value());
    }
}
