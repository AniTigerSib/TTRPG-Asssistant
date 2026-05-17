package ttrpg.CharManagementService.domain.auth.RefreshToken;

import java.net.InetAddress;
import java.time.Instant;

import ttrpg.CharManagementService.domain.user.UserId;

public record RefreshTokenSnapshot(
    RefreshTokenId id,
    UserId userId,
    String tokenHash,
    String userAgent,
    InetAddress ipAddress,
    Instant expiresAt,
    Instant revokedAt,
    Instant createdAt
) {}
