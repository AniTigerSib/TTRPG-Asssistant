package ttrpg.CharManagementService.domain.auth.RefreshToken;

import java.net.Inet4Address;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

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
    private Inet4Address ipAddress;
    private Instant expiresAt;
    private Instant revokedAt;
    private Instant createdAt;

    private RefreshToken(RefreshTokenId id, UserId userId, String tokenHash,
                         String userAgent, Inet4Address ipAddress, Instant expiresAt,
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

    public static RefreshToken create(UserId userId, String userAgent, Inet4Address ipAddress, Instant expiresAt) {
        Instant now = Instant.now();
        return new RefreshToken(
            RefreshTokenId.newId(),
            userId,
            generateTokenHash(generateRawRefreshToken()),
            userAgent,
            ipAddress,
            expiresAt,
            null,
            now
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

    public RefreshTokenId id() { return id; }

    public UserId userId() { return userId; }

    public String tokenHash() { return tokenHash; }

    public String userAgent() { return userAgent; }

    public Inet4Address ipAddress() { return ipAddress; }

    public Instant expiresAt() { return expiresAt; }

    public Instant revokedAt() { return revokedAt; }

    public Instant createdAt() { return createdAt; }

    public boolean isExpired() { return expiresAt.isBefore(Instant.now()); }

    public boolean isRevoked() { return revokedAt != null; }

    public void revoke() { revokedAt = Instant.now(); }

    public boolean isValid() {
        if (isRevoked())
            return false;

        if (isExpired()) {
            revoke();
            return false;
        }
        return true;
    }

    private static String generateTokenHash(String rawRefreshToken) {
        String token = Checkers.requireStringNonBlank(rawRefreshToken, "rawRefreshToken");
        MessageDigest digest = SHA_256.get();
        digest.reset();
        return HEX_FORMAT.formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
    }

    private static String generateRawRefreshToken() {
        byte[] tokenBytes = new byte[32];
        SECURE_RANDOM.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
}
