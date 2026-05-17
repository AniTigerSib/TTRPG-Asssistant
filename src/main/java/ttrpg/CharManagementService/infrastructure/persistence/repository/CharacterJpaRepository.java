package ttrpg.CharManagementService.infrastructure.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ttrpg.CharManagementService.infrastructure.persistence.entity.CharacterJpaEntity;

public interface CharacterJpaRepository extends JpaRepository<CharacterJpaEntity, UUID> {

    @Query("""
        select c from CharacterJpaEntity c
        where c.ownerId = :userId
           or c.campaignId in (
               select cm.id.campaignId from CampaignMemberJpaEntity cm
               where cm.id.userId = :userId
           )
           or c.campaignId in (
               select cp.id from CampaignJpaEntity cp
               where cp.ownerId = :userId
           )
        order by c.updatedAt desc
        """)
    List<CharacterJpaEntity> findAccessibleByUserId(@Param("userId") UUID userId);
}
