package ttrpg.CharManagementService.application.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import ttrpg.CharManagementService.domain.auth.PasswordHasher;
import ttrpg.CharManagementService.domain.auth.PasswordPolicy;
import ttrpg.CharManagementService.domain.auth.PasswordPolicyContext;
import ttrpg.CharManagementService.domain.exception.ExternalExceptions.UserAlreadyExistsException;
import ttrpg.CharManagementService.domain.user.User;
import ttrpg.CharManagementService.domain.user.UserId;
import ttrpg.CharManagementService.domain.user.UserRepository;

class RegisterUserUseCaseTest {

    @Test
    void registersUserWithHashedPassword() {
        var repository = new InMemoryUserRepository();
        var policy = new AcceptAllPasswordPolicy();
        var hasher = new PrefixPasswordHasher();
        var useCase = new RegisterUserUseCase(repository, policy, hasher);

        var user = useCase.execute(new RegisterUserCommand("test@example.com", "hero", "Password1"));

        assertEquals("test@example.com", user.getEmail());
        assertEquals("hero", user.getUsername());
        assertEquals("hashed:Password1", user.getPasswordHash());
        assertEquals(1, repository.storage.size());
    }

    @Test
    void rejectsDuplicateEmail() {
        var repository = new InMemoryUserRepository();
        repository.save(User.create("test@example.com", "hero", "hashed:Password1"));
        var useCase = new RegisterUserUseCase(repository, new AcceptAllPasswordPolicy(), new PrefixPasswordHasher());

        assertThrows(
            UserAlreadyExistsException.class,
            () -> useCase.execute(new RegisterUserCommand("test@example.com", "other", "Password1"))
        );
    }

    private static final class AcceptAllPasswordPolicy implements PasswordPolicy {
        @Override
        public void validate(String rawPassword, PasswordPolicyContext context) {
        }
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
