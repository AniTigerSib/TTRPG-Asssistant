package ttrpg.CharManagementService.domain.gamesystem;

import java.time.Instant;

import ttrpg.CharManagementService.domain.shared.Checkers;

public class GameSystem {
    private final GameSystemId id;
    private final String code;
    private final String name;
    private final String version;
    private final String description;
    private final Instant createdAt;

    private GameSystem(
        GameSystemId id,
        String code,
        String name,
        String version,
        String description,
        Instant createdAt
    ) {
        this.id = Checkers.requireNonNull(id, "id");
        this.code = GameSystemCodes.normalize(code);
        this.name = Checkers.requireStringNonBlank(name, "name");
        this.version = Checkers.requireStringNonBlank(version, "version");
        this.description = normalizeDescription(description);
        this.createdAt = Checkers.requireNonNull(createdAt, "createdAt");
    }

    public static GameSystem create(String code, String name, String version, String description) {
        return new GameSystem(
            GameSystemId.newId(),
            code,
            name,
            version,
            description,
            Instant.now()
        );
    }

    public static GameSystem restore(GameSystemSnapshot snapshot) {
        Checkers.requireNonNull(snapshot, "snapshot");
        return new GameSystem(
            snapshot.id(),
            snapshot.code(),
            snapshot.name(),
            snapshot.version(),
            snapshot.description(),
            snapshot.createdAt()
        );
    }

    public GameSystemId getId() { return id; }

    public String getCode() { return code; }

    public String getName() { return name; }

    public String getVersion() { return version; }

    public String getDescription() { return description; }

    public Instant getCreatedAt() { return createdAt; }

    public GameSystemSnapshot snapshot() {
        return new GameSystemSnapshot(id, code, name, version, description, createdAt);
    }

    private static String normalizeDescription(String description) {
        return description == null || description.isBlank() ? null : description.trim();
    }
}
