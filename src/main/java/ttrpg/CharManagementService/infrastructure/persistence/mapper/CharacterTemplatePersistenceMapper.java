package ttrpg.CharManagementService.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import ttrpg.CharManagementService.domain.chartemplate.CharacterTemplate;
import ttrpg.CharManagementService.domain.chartemplate.CharacterTemplateId;
import ttrpg.CharManagementService.domain.chartemplate.CharacterTemplateSnapshot;
import ttrpg.CharManagementService.domain.chartemplate.CharacterTemplateVisibility;
import ttrpg.CharManagementService.domain.gamesystem.GameSystemId;
import ttrpg.CharManagementService.infrastructure.persistence.entity.CharacterTemplateJpaEntity;

@Component
public class CharacterTemplatePersistenceMapper {

    public CharacterTemplate toDomain(CharacterTemplateJpaEntity entity) {
        return CharacterTemplate.restore(
            new CharacterTemplateSnapshot(
                new CharacterTemplateId(entity.getId()),
                new GameSystemId(entity.getGameSystemId()),
                entity.getName(),
                entity.getSchema(),
                entity.getVersion(),
                entity.isOfficial(),
                CharacterTemplateVisibility.fromDatabaseValue(entity.getVisibility()),
                entity.getCreatedAt()
            )
        );
    }

    public CharacterTemplateJpaEntity toEntity(CharacterTemplate template) {
        var snapshot = template.snapshot();
        return CharacterTemplateJpaEntity.builder()
            .id(snapshot.id().value())
            .gameSystemId(snapshot.gameSystemId().value())
            .name(snapshot.name())
            .schema(snapshot.schema())
            .version(snapshot.version())
            .official(snapshot.official())
            .visibility(snapshot.visibility().toDatabaseValue())
            .createdAt(snapshot.createdAt())
            .build();
    }
}
