package ttrpg.CharManagementService.domain.auth.AccessToken;

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
import ttrpg.CharManagementService.domain.uuid.TokenId;

public class AccessToken {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final ThreadLocal<MessageDigest> SHA_256 = ThreadLocal.withInitial(() -> {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException exception) {
            throw new InvariantViolationException("SHA-256 algorithm is not available", exception);
        }
    });
    private static final HexFormat HEX_FORMAT = HexFormat.of();

    private final TokenId id;
    private final UserId userId;
    private final String tokenHash;
    private final Instant expiresAt;
    private final Instant createdAt;

    private AccessToken(
        TokenId id,
        UserId userId,
        String tokenHash,
        Instant expiresAt,
        Instant createdAt
    ) {
        this.id = Checkers.requireNonNull(id, "id");
        this.userId = Checkers.requireNonNull(userId, "userId");
        this.tokenHash = Checkers.requireStringNonBlank(tokenHash, "tokenHash");
        this.expiresAt = Checkers.requireNonNull(expiresAt, "expiresAt");
        this.createdAt = Checkers.requireNonNull(createdAt, "createdAt");
    }

    public static IssuedAccessToken issue(UserId userId, Instant expiresAt) {
        Checkers.requireNonNull(userId, "userId");
        Checkers.requireNonNull(expiresAt, "expiresAt");

        var id = TokenId.newId();
        var rawToken = rawToken(id);
        var now = Instant.now();

        return new IssuedAccessToken(
            rawToken,
            new AccessToken(id, userId, hash(rawToken), expiresAt, now)
        );
    }

    public static AccessToken restore(AccessTokenSnapshot snapshot) {
        Checkers.requireNonNull(snapshot, "snapshot");
        return new AccessToken(
            snapshot.id(),
            snapshot.userId(),
            snapshot.tokenHash(),
            snapshot.expiresAt(),
            snapshot.createdAt()
        );
    }

    public static TokenId extractTokenId(String rawToken) {
        var value = Checkers.requireStringNonBlank(rawToken, "rawToken");
        var separatorIndex = value.indexOf('.');
        if (separatorIndex <= 0) {
            throw new InvalidTokenException("Invalid access token");
        }
        try {
            return TokenId.fromString(value.substring(0, separatorIndex));
        } catch (RuntimeException exception) {
            throw new InvalidTokenException("Invalid access token");
        }
    }

    public AccessTokenSnapshot snapshot() {
        return new AccessTokenSnapshot(id, userId, tokenHash, expiresAt, createdAt);
    }

    public TokenId id() {
        return id;
    }

    public UserId userId() {
        return userId;
    }

    public Instant expiresAt() {
        return expiresAt;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public boolean matches(String rawToken) {
        return tokenHash.equals(hash(rawToken));
    }

    public boolean isExpired() {
        return !expiresAt.isAfter(Instant.now());
    }

    private static String rawToken(TokenId id) {
        return id.value() + "." + generateTokenSecret();
    }

    private static String generateTokenSecret() {
        var tokenBytes = new byte[32];
        SECURE_RANDOM.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    private static String hash(String rawToken) {
        var token = Checkers.requireStringNonBlank(rawToken, "rawToken");
        var digest = SHA_256.get();
        digest.reset();
        return HEX_FORMAT.formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
    }
}
