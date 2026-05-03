package ttrpg.CharManagementService.domain.auth.RefreshToken;

import java.net.Inet4Address;
import java.time.Instant;

import ttrpg.CharManagementService.domain.user.UserId;

public record RefreshTokenSnapshot(
    RefreshTokenId id,
    UserId userId,
    String tokenHash,
    String userAgent,
    Inet4Address ipAddress,
    Instant expiresAt,
    Instant revokedAt,
    Instant createdAt
){}
