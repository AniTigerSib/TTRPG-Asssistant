package ttrpg.CharManagementService.domain.auth.RefreshToken;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

import ttrpg.CharManagementService.domain.exception.InvalidTokenException;
import ttrpg.CharManagementService.domain.exception.InvariantViolationException;
import ttrpg.CharManagementService.domain.shared.Checkers;
import ttrpg.CharManagementService.domain.user.UserId;

public class RefreshToken {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final ThreadLocal<MessageDigest> SHA_256 = ThreadLocal.withInitial(() -> {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new InvariantViolationException("SHA-256 algorithm is not available", e);
        }
    });
    private static final HexFormat HEX_FORMAT = HexFormat.of();

    private RefreshTokenId id;
    private UserId userId;
    private String tokenHash;
    private String userAgent;
    private InetAddress ipAddress;
    private Instant expiresAt;
    private Instant revokedAt;
    private Instant createdAt;

    private RefreshToken(RefreshTokenId id, UserId userId, String tokenHash,
                         String userAgent, InetAddress ipAddress, Instant expiresAt,
                         Instant revokedAt, Instant createdAt) {
        this.id = Checkers.requireNonNull(id, "id");
        this.userId = Checkers.requireNonNull(userId, "userId");
        this.tokenHash = Checkers.requireStringNonBlank(tokenHash, "tokenHash");
        this.userAgent = Checkers.requireStringNonBlank(userAgent, "userAgent");
        this.ipAddress = Checkers.requireNonNull(ipAddress, "ipAddress");
        this.expiresAt = Checkers.requireNonNull(expiresAt, "expiresAt");
        this.revokedAt = revokedAt;
        this.createdAt = Checkers.requireNonNull(createdAt, "createdAt");
    }

    public static IssuedRefreshToken issue(UserId userId, String userAgent, InetAddress ipAddress, Instant expiresAt) {
        Checkers.requireNonNull(userId, "userId");
        Checkers.requireStringNonBlank(userAgent, "userAgent");
        Checkers.requireNonNull(ipAddress, "ipAddress");
        Checkers.requireNonNull(expiresAt, "expiresAt");

        var id = RefreshTokenId.newId();
        var rawToken = rawToken(id);
        var now = Instant.now();

        return new IssuedRefreshToken(
            rawToken,
            new RefreshToken(
                id,
                userId,
                hash(rawToken),
                userAgent,
                ipAddress,
                expiresAt,
                null,
                now
            )
        );
    }

    public static RefreshToken restore(RefreshTokenSnapshot snapshot) {
        Checkers.requireNonNull(snapshot, "snapshot");
        return new RefreshToken(
            snapshot.id(),
            snapshot.userId(),
            snapshot.tokenHash(),
            snapshot.userAgent(),
            snapshot.ipAddress(),
            snapshot.expiresAt(),
            snapshot.revokedAt(),
            snapshot.createdAt()
        );
    }

    public static RefreshTokenId extractTokenId(String rawToken) {
        var value = Checkers.requireStringNonBlank(rawToken, "rawToken");
        var separatorIndex = value.indexOf('.');
        if (separatorIndex <= 0) {
            throw new InvalidTokenException("Invalid refresh token");
        }
        try {
            return RefreshTokenId.fromString(value.substring(0, separatorIndex));
        } catch (RuntimeException exception) {
            throw new InvalidTokenException("Invalid refresh token");
        }
    }

    public RefreshTokenSnapshot snapshot() {
        return new RefreshTokenSnapshot(
            id,
            userId,
            tokenHash,
            userAgent,
            ipAddress,
            expiresAt,
            revokedAt,
            createdAt
        );
    }

    public RefreshTokenId getId() { return id; }

    public UserId getUserId() { return userId; }

    public String getTokenHash() { return tokenHash; }

    public String getUserAgent() { return userAgent; }

    public InetAddress getIpAddress() { return ipAddress; }

    public Instant getExpiresAt() { return expiresAt; }

    public Instant getRevokedAt() { return revokedAt; }

    public Instant getCreatedAt() { return createdAt; }

    public boolean isExpired() { return expiresAt.isBefore(Instant.now()); }

    public boolean isRevoked() { return revokedAt != null; }

    public boolean matches(String rawToken) {
        return tokenHash.equals(hash(rawToken));
    }

    public void revoke() {
        if (!isRevoked()) {
            revokedAt = Instant.now();
        }
    }

    public boolean isValid() {
        if (isRevoked())
            return false;

        if (isExpired()) {
            revoke();
            return false;
        }
        return true;
    }

    private static String rawToken(RefreshTokenId id) {
        return id.value() + "." + generateTokenSecret();
    }

    private static String hash(String rawRefreshToken) {
        String token = Checkers.requireStringNonBlank(rawRefreshToken, "rawRefreshToken");
        MessageDigest digest = SHA_256.get();
        digest.reset();
        return HEX_FORMAT.formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
    }

    private static String generateTokenSecret() {
        byte[] tokenBytes = new byte[32];
        SECURE_RANDOM.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
}
