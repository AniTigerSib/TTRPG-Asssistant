package ttrpg.CharManagementService.application.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ttrpg.CharManagementService.domain.auth.AccessToken.AccessToken;
import ttrpg.CharManagementService.domain.auth.AccessToken.AccessTokenRepository;
import ttrpg.CharManagementService.domain.auth.RefreshToken.RefreshToken;
import ttrpg.CharManagementService.domain.auth.RefreshToken.RefreshTokenId;
import ttrpg.CharManagementService.domain.auth.RefreshToken.RefreshTokenRepository;
import ttrpg.CharManagementService.domain.exception.InvalidTokenException;
import ttrpg.CharManagementService.domain.user.UserId;
import ttrpg.CharManagementService.domain.uuid.TokenId;
import ttrpg.CharManagementService.infrastructure.security.config.AuthTokenProperties;

class AuthenticationTokenServiceTest {

    private InMemoryAccessTokenRepository accessTokenRepository;
    private InMemoryRefreshTokenRepository refreshTokenRepository;
    private AuthenticationTokenService authenticationTokenService;

    @BeforeEach
    void setUp() {
        accessTokenRepository = new InMemoryAccessTokenRepository();
        refreshTokenRepository = new InMemoryRefreshTokenRepository();
        authenticationTokenService = new AuthenticationTokenService(
            accessTokenRepository,
            refreshTokenRepository,
            new AuthTokenProperties(Duration.ofMinutes(15), Duration.ofDays(30))
        );
    }

    @Test
    void issuesAccessAndRefreshTokens() {
        var userId = UserId.newId();

        var tokens = authenticationTokenService.issueTokens(
            userId,
            "JUnit",
            InetAddress.getLoopbackAddress()
        );

        assertTrue(tokens.accessToken().contains("."));
        assertTrue(tokens.refreshToken().contains("."));
        assertEquals(1, accessTokenRepository.storage.size());
        assertEquals(1, refreshTokenRepository.storage.size());
        assertTrue(tokens.accessTokenExpiresAt().isAfter(Instant.now()));
        assertTrue(tokens.refreshTokenExpiresAt().isAfter(tokens.accessTokenExpiresAt()));
    }

    @Test
    void refreshesTokensAndRevokesPreviousRefreshToken() {
        var userId = UserId.newId();
        var originalTokens = authenticationTokenService.issueTokens(
            userId,
            "JUnit",
            InetAddress.getLoopbackAddress()
        );
        var originalRefreshToken = refreshTokenRepository.findByRawToken(originalTokens.refreshToken()).orElseThrow();

        var refreshed = authenticationTokenService.refreshTokens(
            originalTokens.refreshToken(),
            "JUnit-Refreshed",
            InetAddress.getLoopbackAddress()
        );

        var revokedOriginal = refreshTokenRepository.findById(originalRefreshToken.getId()).orElseThrow();
        assertEquals(userId, refreshed.userId());
        assertTrue(revokedOriginal.isRevoked());
        assertFalse(refreshed.tokens().refreshToken().equals(originalTokens.refreshToken()));
        assertEquals(2, refreshTokenRepository.storage.size());
        assertEquals(2, accessTokenRepository.storage.size());
    }

    @Test
    void rejectsRefreshWhenTokenValueDoesNotMatchStoredHash() {
        var userId = UserId.newId();
        var tokens = authenticationTokenService.issueTokens(
            userId,
            "JUnit",
            InetAddress.getLoopbackAddress()
        );

        var invalidToken = tokens.refreshToken().replaceFirst("\\.[^.]+$", ".tampered");

        assertThrows(
            InvalidTokenException.class,
            () -> authenticationTokenService.refreshTokens(
                invalidToken,
                "JUnit",
                InetAddress.getLoopbackAddress()
            )
        );
    }

    @Test
    void rejectsRefreshWhenTokenAlreadyRevoked() {
        var userId = UserId.newId();
        var tokens = authenticationTokenService.issueTokens(
            userId,
            "JUnit",
            InetAddress.getLoopbackAddress()
        );
        var stored = refreshTokenRepository.findByRawToken(tokens.refreshToken()).orElseThrow();
        stored.revoke();
        refreshTokenRepository.save(stored);

        assertThrows(
            InvalidTokenException.class,
            () -> authenticationTokenService.refreshTokens(
                tokens.refreshToken(),
                "JUnit",
                InetAddress.getLoopbackAddress()
            )
        );
    }

    @Test
    void resolvesUserIdByValidAccessToken() {
        var userId = UserId.newId();
        var tokens = authenticationTokenService.issueTokens(
            userId,
            "JUnit",
            InetAddress.getLoopbackAddress()
        );

        var resolved = authenticationTokenService.resolveUserIdByAccessToken(tokens.accessToken());

        assertTrue(resolved.isPresent());
        assertEquals(userId, resolved.orElseThrow());
    }

    @Test
    void logoutDeletesAccessTokenAndRevokesRefreshToken() {
        var userId = UserId.newId();
        var tokens = authenticationTokenService.issueTokens(
            userId,
            "JUnit",
            InetAddress.getLoopbackAddress()
        );

        authenticationTokenService.logout(userId, tokens.accessToken(), tokens.refreshToken());

        assertTrue(accessTokenRepository.findById(AccessToken.extractTokenId(tokens.accessToken())).isEmpty());
        assertTrue(refreshTokenRepository.findByRawToken(tokens.refreshToken()).orElseThrow().isRevoked());
    }

    @Test
    void returnsEmptyAndDeletesExpiredAccessToken() {
        var userId = UserId.newId();
        var issued = AccessToken.issue(userId, Instant.now().minusSeconds(5));
        accessTokenRepository.save(issued.accessToken());

        var resolved = authenticationTokenService.resolveUserIdByAccessToken(issued.rawToken());

        assertTrue(resolved.isEmpty());
        assertTrue(accessTokenRepository.findById(issued.accessToken().id()).isEmpty());
    }

    private static final class InMemoryAccessTokenRepository implements AccessTokenRepository {
        private final Map<TokenId, AccessToken> storage = new HashMap<>();

        @Override
        public AccessToken save(AccessToken accessToken) {
            storage.put(accessToken.id(), accessToken);
            return accessToken;
        }

        @Override
        public Optional<AccessToken> findById(TokenId id) {
            return Optional.ofNullable(storage.get(id));
        }

        @Override
        public void deleteById(TokenId id) {
            storage.remove(id);
        }
    }

    private static final class InMemoryRefreshTokenRepository implements RefreshTokenRepository {
        private final Map<RefreshTokenId, RefreshToken> storage = new HashMap<>();

        @Override
        public RefreshToken save(RefreshToken refreshToken) {
            storage.put(refreshToken.getId(), refreshToken);
            return refreshToken;
        }

        @Override
        public Optional<RefreshToken> findById(RefreshTokenId id) {
            return Optional.ofNullable(storage.get(id));
        }

        private Optional<RefreshToken> findByRawToken(String rawToken) {
            return storage.values().stream()
                .filter(token -> token.matches(rawToken))
                .findFirst();
        }
    }
}
