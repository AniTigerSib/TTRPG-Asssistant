package ttrpg.CharManagementService.domain.user;

import java.time.Instant;
import java.util.Set;

public record UserSnapshot(
    UserId id,
    String email,
    String username,
    String passwordHash,
    Set<UserRole> roles,
    Instant createdAt,
    Instant updatedAt
) {}
