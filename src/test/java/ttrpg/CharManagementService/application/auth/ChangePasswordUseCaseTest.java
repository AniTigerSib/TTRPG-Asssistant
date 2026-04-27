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
import ttrpg.CharManagementService.domain.exception.ExternalExceptions.InvalidCredentailsException;
import ttrpg.CharManagementService.domain.exception.ExternalExceptions.InvalidPasswordException;
import ttrpg.CharManagementService.domain.user.User;
import ttrpg.CharManagementService.domain.user.UserId;
import ttrpg.CharManagementService.domain.user.UserRepository;

class ChangePasswordUseCaseTest {

    @Test
    void changesPasswordWhenCurrentPasswordMatches() {
        var repository = new InMemoryUserRepository();
        var user = User.create("test@example.com", "hero", "hashed:Password1");
        repository.save(user);

        var useCase = new ChangePasswordUseCase(repository, new AcceptAllPasswordPolicy(), new PrefixPasswordHasher());

        useCase.execute(new ChangePasswordCommand(user.getId().value(), "Password1", "BetterPass2"));

        assertEquals("hashed:BetterPass2", repository.storage.get(user.getId()).getPasswordHash());
    }

    @Test
    void rejectsInvalidCurrentPassword() {
        var repository = new InMemoryUserRepository();
        var user = User.create("test@example.com", "hero", "hashed:Password1");
        repository.save(user);

        var useCase = new ChangePasswordUseCase(repository, new AcceptAllPasswordPolicy(), new PrefixPasswordHasher());

        assertThrows(
            InvalidCredentailsException.class,
            () -> useCase.execute(new ChangePasswordCommand(user.getId().value(), "Wrong1", "BetterPass2"))
        );
    }

    @Test
    void rejectsReusingCurrentPassword() {
        var repository = new InMemoryUserRepository();
        var user = User.create("test@example.com", "hero", "hashed:Password1");
        repository.save(user);

        var useCase = new ChangePasswordUseCase(repository, new AcceptAllPasswordPolicy(), new PrefixPasswordHasher());

        assertThrows(
            InvalidPasswordException.class,
            () -> useCase.execute(new ChangePasswordCommand(user.getId().value(), "Password1", "Password1"))
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
            return Optional.empty();
        }

        @Override
        public Optional<User> findByUsername(String username) {
            return Optional.empty();
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
            return false;
        }

        @Override
        public boolean existsByUsername(String username) {
            return false;
        }
    }
}
