package ttrpg.CharManagementService.domain.campaign;

import java.time.Instant;

import ttrpg.CharManagementService.domain.gamesystem.GameSystemId;
import ttrpg.CharManagementService.domain.shared.Checkers;
import ttrpg.CharManagementService.domain.user.UserId;

public class Campaign {
    private final CampaignId id;
    private final UserId ownerId;
    private final GameSystemId gameSystemId;
    private final String name;
    private final String description;
    private final CampaignVisibility visibility;
    private final Instant createdAt;
    private final Instant updatedAt;

    private Campaign(
        CampaignId id,
        UserId ownerId,
        GameSystemId gameSystemId,
        String name,
        String description,
        CampaignVisibility visibility,
        Instant createdAt,
        Instant updatedAt
    ) {
        this.id = Checkers.requireNonNull(id, "id");
        this.ownerId = Checkers.requireNonNull(ownerId, "ownerId");
        this.gameSystemId = Checkers.requireNonNull(gameSystemId, "gameSystemId");
        this.name = Checkers.requireStringNonBlank(name, "name");
        this.description = description == null || description.isBlank() ? null : description.trim();
        this.visibility = Checkers.requireNonNull(visibility, "visibility");
        this.createdAt = Checkers.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Checkers.requireNonNull(updatedAt, "updatedAt");
    }

    public static Campaign restore(CampaignSnapshot snapshot) {
        Checkers.requireNonNull(snapshot, "snapshot");
        return new Campaign(
            snapshot.id(),
            snapshot.ownerId(),
            snapshot.gameSystemId(),
            snapshot.name(),
            snapshot.description(),
            snapshot.visibility(),
            snapshot.createdAt(),
            snapshot.updatedAt()
        );
    }

    public CampaignId getId() { return id; }

    public UserId getOwnerId() { return ownerId; }

    public GameSystemId getGameSystemId() { return gameSystemId; }

    public CampaignVisibility getVisibility() { return visibility; }
}
