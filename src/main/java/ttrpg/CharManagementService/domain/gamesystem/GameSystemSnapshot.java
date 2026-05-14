package ttrpg.CharManagementService.domain.gamesystem;

import java.time.Instant;

public record GameSystemSnapshot(
    GameSystemId id,
    String code,
    String name,
    String version,
    String description,
    Instant createdAt
) {}
