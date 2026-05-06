package ttrpg.CharManagementService.application.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.InetAddress;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import ttrpg.CharManagementService.domain.auth.PasswordHasher;
import ttrpg.CharManagementService.domain.exception.InvalidCredentialsException;
import ttrpg.CharManagementService.domain.user.User;
import ttrpg.CharManagementService.domain.user.UserId;
import ttrpg.CharManagementService.domain.user.UserRepository;

class LoginUserUseCaseTest {

    @Test
    void authenticatesUserByEmail() {
        var repository = new InMemoryUserRepository();
        var user = User.create("test@example.com", "hero", "hashed:Password1");
        repository.save(user);

        var useCase = new LoginUserUseCase(
            repository,
            new PrefixPasswordHasher(),
            new StubAuthenticationTokenService()
        );

        var authenticated = useCase.execute(
            new LoginUserCommand("test@example.com", "Password1", "JUnit", loopbackAddress())
        );

        assertEquals(user.getId(), authenticated.user().getId());
        assertNotNull(authenticated.tokens().accessToken());
    }

    @Test
    void authenticatesUserByUsername() {
        var repository = new InMemoryUserRepository();
        var user = User.create("test@example.com", "hero", "hashed:Password1");
        repository.save(user);

        var useCase = new LoginUserUseCase(
            repository,
            new PrefixPasswordHasher(),
            new StubAuthenticationTokenService()
        );

        var authenticated = useCase.execute(
            new LoginUserCommand("hero", "Password1", "JUnit", loopbackAddress())
        );

        assertEquals(user.getEmail(), authenticated.user().getEmail());
    }

    @Test
    void rejectsInvalidCredentials() {
        var repository = new InMemoryUserRepository();
        repository.save(User.create("test@example.com", "hero", "hashed:Password1"));

        var useCase = new LoginUserUseCase(
            repository,
            new PrefixPasswordHasher(),
            new StubAuthenticationTokenService()
        );

        assertThrows(
            InvalidCredentialsException.class,
            () -> useCase.execute(new LoginUserCommand("hero", "Wrong1", "JUnit", loopbackAddress()))
        );
    }

    private static InetAddress loopbackAddress() {
        return InetAddress.getLoopbackAddress();
    }

    private static final class PrefixPasswordHasher implements PasswordHasher {
        @Override
        public String hash(String rawPassword) {
            return "hashed:" + rawPassword;
        }

        @Override
        public boolean matches(String rawPassword, String hashedPassword) {
            return hash(rawPassword).equals(hashedPassword);
        }
    }

    private static final class StubAuthenticationTokenService extends AuthenticationTokenService {
        private StubAuthenticationTokenService() {
            super(null, null, null);
        }

        @Override
        public AuthenticationTokens issueTokens(UserId userId, String userAgent, InetAddress ipAddress) {
            return new AuthenticationTokens(
                "access-token",
                Instant.parse("2026-05-06T10:15:30Z"),
                "refresh-token",
                Instant.parse("2026-06-05T10:15:30Z")
            );
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
