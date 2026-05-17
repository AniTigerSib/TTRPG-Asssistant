package ttrpg.CharManagementService.presentation.dto;

import java.time.Instant;
import java.util.UUID;

public record GameSystemResponse(
    UUID id,
    String code,
    String name,
    String version,
    String description,
    Instant createdAt
) {}
