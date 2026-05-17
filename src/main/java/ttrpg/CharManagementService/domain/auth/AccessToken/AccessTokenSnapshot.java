package ttrpg.CharManagementService.domain.auth.AccessToken;

import java.time.Instant;

import ttrpg.CharManagementService.domain.user.UserId;
import ttrpg.CharManagementService.domain.uuid.TokenId;

public record AccessTokenSnapshot(
    TokenId id,
    UserId userId,
    String tokenHash,
    Instant expiresAt,
    Instant createdAt
) {}
