package ttrpg.CharManagementService.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import ttrpg.CharManagementService.infrastructure.persistence.entity.CharacterDataJpaEntity;

public interface CharacterDataJpaRepository extends JpaRepository<CharacterDataJpaEntity, UUID> {

    Optional<CharacterDataJpaEntity> findByCharacterId(UUID characterId);
}
