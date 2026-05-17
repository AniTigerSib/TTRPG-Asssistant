package ttrpg.CharManagementService.infrastructure.persistence.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import ttrpg.CharManagementService.infrastructure.persistence.entity.RefreshTokenJpaEntity;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenJpaEntity, UUID> {
}
