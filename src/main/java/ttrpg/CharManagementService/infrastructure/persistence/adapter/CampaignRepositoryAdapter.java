package ttrpg.CharManagementService.infrastructure.persistence.adapter;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import ttrpg.CharManagementService.domain.campaign.Campaign;
import ttrpg.CharManagementService.domain.campaign.CampaignId;
import ttrpg.CharManagementService.domain.campaign.CampaignRepository;
import ttrpg.CharManagementService.infrastructure.persistence.mapper.CampaignPersistenceMapper;
import ttrpg.CharManagementService.infrastructure.persistence.repository.CampaignJpaRepository;

@Repository
@RequiredArgsConstructor
public class CampaignRepositoryAdapter implements CampaignRepository {

    private final CampaignJpaRepository jpaRepository;
    private final CampaignPersistenceMapper mapper;

    @Override
    public Optional<Campaign> findById(CampaignId id) {
        return jpaRepository.findById(id.value()).map(mapper::toDomain);
    }
}
