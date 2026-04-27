package ttrpg.CharManagementService.presentation.dto;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record UserResponse(
    UUID id,
    String email,
    String username,
    Set<String> roles,
    Instant createdAt,
    Instant updatedAt
) {}
