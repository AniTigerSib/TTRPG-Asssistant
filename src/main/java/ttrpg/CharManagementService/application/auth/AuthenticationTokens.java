package ttrpg.CharManagementService.application.auth;

import java.time.Instant;

public record AuthenticationTokens(
    String accessToken,
    Instant accessTokenExpiresAt,
    String refreshToken,
    Instant refreshTokenExpiresAt
) {}
