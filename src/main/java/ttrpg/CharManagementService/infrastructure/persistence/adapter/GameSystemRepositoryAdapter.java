package ttrpg.CharManagementService.infrastructure.persistence.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import ttrpg.CharManagementService.domain.gamesystem.GameSystem;
import ttrpg.CharManagementService.domain.gamesystem.GameSystemId;
import ttrpg.CharManagementService.domain.gamesystem.GameSystemRepository;
import ttrpg.CharManagementService.infrastructure.persistence.mapper.GameSystemPersistenceMapper;
import ttrpg.CharManagementService.infrastructure.persistence.repository.GameSystemJpaRepository;

@Repository
@RequiredArgsConstructor
public class GameSystemRepositoryAdapter implements GameSystemRepository {

    private final GameSystemJpaRepository jpaRepository;
    private final GameSystemPersistenceMapper mapper;

    @Override
    public List<GameSystem> findAll() {
        return jpaRepository.findAllByOrderByNameAscVersionAsc().stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public Optional<GameSystem> findById(GameSystemId id) {
        return jpaRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public Optional<GameSystem> findByCode(String code) {
        return jpaRepository.findByCodeIgnoreCase(code).map(mapper::toDomain);
    }
}
