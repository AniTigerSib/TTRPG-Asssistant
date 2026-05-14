package ttrpg.CharManagementService.presentation.dto;

import java.time.Instant;
import java.util.UUID;

public record CharacterTemplateResponse(
    UUID id,
    UUID gameSystemId,
    String name,
    int version,
    boolean official,
    String visibility,
    Object schema,
    Instant createdAt
) {}
