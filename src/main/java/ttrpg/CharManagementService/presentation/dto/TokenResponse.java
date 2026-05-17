package ttrpg.CharManagementService.presentation.dto;

import java.time.Instant;

public record TokenResponse(
    String token,
    Instant expiresAt
) {}
