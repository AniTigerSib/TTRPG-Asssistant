package ttrpg.CharManagementService.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import ttrpg.CharManagementService.domain.character.CharacterId;
import ttrpg.CharManagementService.domain.characterdata.CharacterData;
import ttrpg.CharManagementService.domain.characterdata.CharacterDataId;
import ttrpg.CharManagementService.domain.characterdata.CharacterDataSnapshot;
import ttrpg.CharManagementService.infrastructure.persistence.entity.CharacterDataJpaEntity;

@Component
public class CharacterDataPersistenceMapper {

    public CharacterData toDomain(CharacterDataJpaEntity entity) {
        return CharacterData.restore(
            new CharacterDataSnapshot(
                new CharacterDataId(entity.getId()),
                new CharacterId(entity.getCharacterId()),
                entity.getData(),
                entity.getVersion(),
                entity.getUpdatedAt()
            )
        );
    }

    public CharacterDataJpaEntity toEntity(CharacterData characterData) {
        var snapshot = characterData.snapshot();
        return CharacterDataJpaEntity.builder()
            .id(snapshot.id().value())
            .characterId(snapshot.characterId().value())
            .data(snapshot.data())
            .version(snapshot.version())
            .updatedAt(snapshot.updatedAt())
            .build();
    }
}
