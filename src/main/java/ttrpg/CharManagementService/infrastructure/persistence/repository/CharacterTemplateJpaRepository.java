package ttrpg.CharManagementService.infrastructure.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import ttrpg.CharManagementService.infrastructure.persistence.entity.CharacterTemplateJpaEntity;

public interface CharacterTemplateJpaRepository extends JpaRepository<CharacterTemplateJpaEntity, UUID> {

    List<CharacterTemplateJpaEntity> findByGameSystemIdOrderByOfficialDescVersionDescNameAsc(UUID gameSystemId);
}
