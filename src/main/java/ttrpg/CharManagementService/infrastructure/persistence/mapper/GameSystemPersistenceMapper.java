package ttrpg.CharManagementService.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import ttrpg.CharManagementService.domain.gamesystem.GameSystem;
import ttrpg.CharManagementService.domain.gamesystem.GameSystemId;
import ttrpg.CharManagementService.domain.gamesystem.GameSystemSnapshot;
import ttrpg.CharManagementService.infrastructure.persistence.entity.GameSystemJpaEntity;

@Component
public class GameSystemPersistenceMapper {

    public GameSystem toDomain(GameSystemJpaEntity entity) {
        return GameSystem.restore(
            new GameSystemSnapshot(
                new GameSystemId(entity.getId()),
                entity.getCode(),
                entity.getName(),
                entity.getVersion(),
                entity.getDescription(),
                entity.getCreatedAt()
            )
        );
    }

    public GameSystemJpaEntity toEntity(GameSystem gameSystem) {
        var snapshot = gameSystem.snapshot();
        return GameSystemJpaEntity.builder()
            .id(snapshot.id().value())
            .code(snapshot.code())
            .name(snapshot.name())
            .version(snapshot.version())
            .description(snapshot.description())
            .createdAt(snapshot.createdAt())
            .build();
    }
}
