package ttrpg.CharManagementService.domain.character;

import java.time.Instant;

import ttrpg.CharManagementService.domain.campaign.CampaignId;
import ttrpg.CharManagementService.domain.chartemplate.CharacterTemplateId;
import ttrpg.CharManagementService.domain.gamesystem.GameSystemId;
import ttrpg.CharManagementService.domain.shared.Checkers;
import ttrpg.CharManagementService.domain.user.UserId;

public class Character {
    private final CharacterId id;
    private final UserId ownerId;
    private final CampaignId campaignId;
    private final GameSystemId gameSystemId;
    private final CharacterTemplateId templateId;
    private String name;
    private String avatarUrl;
    private CharacterStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    private Character(
        CharacterId id,
        UserId ownerId,
        CampaignId campaignId,
        GameSystemId gameSystemId,
        CharacterTemplateId templateId,
        String name,
        String avatarUrl,
        CharacterStatus status,
        Instant createdAt,
        Instant updatedAt
    ) {
        this.id = Checkers.requireNonNull(id, "id");
        this.ownerId = Checkers.requireNonNull(ownerId, "ownerId");
        this.campaignId = campaignId;
        this.gameSystemId = Checkers.requireNonNull(gameSystemId, "gameSystemId");
        this.templateId = templateId;
        this.name = Checkers.requireStringNonBlank(name, "name");
        this.avatarUrl = avatarUrl == null || avatarUrl.isBlank() ? null : avatarUrl.trim();
        this.status = Checkers.requireNonNull(status, "status");
        this.createdAt = Checkers.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Checkers.requireNonNull(updatedAt, "updatedAt");
    }

    public static Character create(
        UserId ownerId,
        CampaignId campaignId,
        GameSystemId gameSystemId,
        CharacterTemplateId templateId,
        String name,
        String avatarUrl,
        CharacterStatus status
    ) {
        return new Character(
            CharacterId.newId(),
            ownerId,
            campaignId,
            gameSystemId,
            templateId,
            name,
            avatarUrl,
            status,
            Instant.now(),
            Instant.now()
        );
    }

    public static Character restore(CharacterSnapshot snapshot) {
        Checkers.requireNonNull(snapshot, "snapshot");
        return new Character(
            snapshot.id(),
            snapshot.ownerId(),
            snapshot.campaignId(),
            snapshot.gameSystemId(),
            snapshot.templateId(),
            snapshot.name(),
            snapshot.avatarUrl(),
            snapshot.status(),
            snapshot.createdAt(),
            snapshot.updatedAt()
        );
    }

    public CharacterId getId() { return id; }

    public UserId getOwnerId() { return ownerId; }

    public CampaignId getCampaignId() { return campaignId; }

    public GameSystemId getGameSystemId() { return gameSystemId; }

    public CharacterTemplateId getTemplateId() { return templateId; }

    public String getName() { return name; }

    public String getAvatarUrl() { return avatarUrl; }

    public CharacterStatus getStatus() { return status; }

    public Instant getCreatedAt() { return createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }

    public void update(String name, String avatarUrl, CharacterStatus status) {
        this.name = Checkers.requireStringNonBlank(name, "name");
        this.avatarUrl = avatarUrl == null || avatarUrl.isBlank() ? null : avatarUrl.trim();
        this.status = Checkers.requireNonNull(status, "status");
        this.updatedAt = Instant.now();
    }

    public CharacterSnapshot snapshot() {
        return new CharacterSnapshot(
            id,
            ownerId,
            campaignId,
            gameSystemId,
            templateId,
            name,
            avatarUrl,
            status,
            createdAt,
            updatedAt
        );
    }
}
