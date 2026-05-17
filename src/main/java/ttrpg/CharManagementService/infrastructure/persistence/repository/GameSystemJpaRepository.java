package ttrpg.CharManagementService.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import ttrpg.CharManagementService.infrastructure.persistence.entity.GameSystemJpaEntity;

public interface GameSystemJpaRepository extends JpaRepository<GameSystemJpaEntity, UUID> {

    List<GameSystemJpaEntity> findAllByOrderByNameAscVersionAsc();

    Optional<GameSystemJpaEntity> findByCodeIgnoreCase(String code);
}
