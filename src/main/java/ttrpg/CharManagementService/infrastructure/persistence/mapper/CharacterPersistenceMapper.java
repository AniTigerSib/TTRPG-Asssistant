package ttrpg.CharManagementService.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import ttrpg.CharManagementService.domain.campaign.CampaignId;
import ttrpg.CharManagementService.domain.character.Character;
import ttrpg.CharManagementService.domain.character.CharacterId;
import ttrpg.CharManagementService.domain.character.CharacterSnapshot;
import ttrpg.CharManagementService.domain.character.CharacterStatus;
import ttrpg.CharManagementService.domain.chartemplate.CharacterTemplateId;
import ttrpg.CharManagementService.domain.gamesystem.GameSystemId;
import ttrpg.CharManagementService.domain.user.UserId;
import ttrpg.CharManagementService.infrastructure.persistence.entity.CharacterJpaEntity;

@Component
public class CharacterPersistenceMapper {

    public Character toDomain(CharacterJpaEntity entity) {
        return Character.restore(
            new CharacterSnapshot(
                new CharacterId(entity.getId()),
                new UserId(entity.getOwnerId()),
                entity.getCampaignId() == null ? null : new CampaignId(entity.getCampaignId()),
                new GameSystemId(entity.getGameSystemId()),
                entity.getTemplateId() == null ? null : new CharacterTemplateId(entity.getTemplateId()),
                entity.getName(),
                entity.getAvatarUrl(),
                CharacterStatus.fromDatabaseValue(entity.getStatus()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
            )
        );
    }

    public CharacterJpaEntity toEntity(Character character) {
        var snapshot = character.snapshot();
        return CharacterJpaEntity.builder()
            .id(snapshot.id().value())
            .ownerId(snapshot.ownerId().value())
            .campaignId(snapshot.campaignId() == null ? null : snapshot.campaignId().value())
            .gameSystemId(snapshot.gameSystemId().value())
            .templateId(snapshot.templateId() == null ? null : snapshot.templateId().value())
            .name(snapshot.name())
            .avatarUrl(snapshot.avatarUrl())
            .status(snapshot.status().toDatabaseValue())
            .createdAt(snapshot.createdAt())
            .updatedAt(snapshot.updatedAt())
            .build();
    }
}
