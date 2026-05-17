package ttrpg.CharManagementService.infrastructure.persistence.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import ttrpg.CharManagementService.infrastructure.persistence.entity.CampaignJpaEntity;

public interface CampaignJpaRepository extends JpaRepository<CampaignJpaEntity, UUID> {
}
