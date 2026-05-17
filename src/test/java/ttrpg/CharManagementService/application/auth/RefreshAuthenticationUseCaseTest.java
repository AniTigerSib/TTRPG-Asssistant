package ttrpg.CharManagementService.application.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.InetAddress;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import ttrpg.CharManagementService.domain.exception.InvalidTokenException;
import ttrpg.CharManagementService.domain.user.User;
import ttrpg.CharManagementService.domain.user.UserId;
import ttrpg.CharManagementService.domain.user.UserRepository;

class RefreshAuthenticationUseCaseTest {

    @Test
    void returnsUserAndRotatedTokensForValidRefreshRequest() {
        var userRepository = new InMemoryUserRepository();
        var user = User.create("test@example.com", "hero", "hashed:Password1");
        userRepository.save(user);

        var useCase = new RefreshAuthenticationUseCase(
            new StubAuthenticationTokenService(user.getId()),
            userRepository
        );

        var result = useCase.execute(
            new RefreshAuthenticationCommand("refresh-token", "JUnit", InetAddress.getLoopbackAddress())
        );

        assertEquals(user.getId(), result.user().getId());
        assertEquals("access-token", result.tokens().accessToken());
        assertEquals("refresh-token-rotated", result.tokens().refreshToken());
    }

    @Test
    void propagatesInvalidTokenErrors() {
        var useCase = new RefreshAuthenticationUseCase(
            new FailingAuthenticationTokenService(),
            new InMemoryUserRepository()
        );

        assertThrows(
            InvalidTokenException.class,
            () -> useCase.execute(
                new RefreshAuthenticationCommand("bad-token", "JUnit", InetAddress.getLoopbackAddress())
            )
        );
    }

    private static final class StubAuthenticationTokenService extends AuthenticationTokenService {
        private final UserId userId;

        private StubAuthenticationTokenService(UserId userId) {
            super(null, null, null);
            this.userId = userId;
        }

        @Override
        public RefreshedAuthenticationTokens refreshTokens(String rawRefreshToken, String userAgent, InetAddress ipAddress) {
            return new RefreshedAuthenticationTokens(
                userId,
                new AuthenticationTokens(
                    "access-token",
                    Instant.parse("2026-05-06T10:15:30Z"),
                    "refresh-token-rotated",
                    Instant.parse("2026-06-05T10:15:30Z")
                )
            );
        }
    }

    private static final class FailingAuthenticationTokenService extends AuthenticationTokenService {
        private FailingAuthenticationTokenService() {
            super(null, null, null);
        }

        @Override
        public RefreshedAuthenticationTokens refreshTokens(String rawRefreshToken, String userAgent, InetAddress ipAddress) {
            throw new InvalidTokenException("Invalid refresh token");
        }
    }

    private static final class InMemoryUserRepository implements UserRepository {
        private final Map<UserId, User> storage = new HashMap<>();

        @Override
        public Optional<User> findById(UserId id) {
            return Optional.ofNullable(storage.get(id));
        }

        @Override
        public Optional<User> findByEmail(String email) {
            return storage.values().stream()
                .filter(user -> user.getEmail().equalsIgnoreCase(email))
                .findFirst();
        }

        @Override
        public Optional<User> findByUsername(String username) {
            return storage.values().stream()
                .filter(user -> user.getUsername().equalsIgnoreCase(username))
                .findFirst();
        }

        @Override
        public User save(User user) {
            storage.put(user.getId(), user);
            return user;
        }

        @Override
        public void delete(User user) {
            storage.remove(user.getId());
        }

        @Override
        public boolean existsById(UserId id) {
            return storage.containsKey(id);
        }

        @Override
        public boolean existsByEmail(String email) {
            return storage.values().stream().anyMatch(user -> user.getEmail().equalsIgnoreCase(email));
        }

        @Override
        public boolean existsByUsername(String username) {
            return storage.values().stream().anyMatch(user -> user.getUsername().equalsIgnoreCase(username));
        }
    }
}
