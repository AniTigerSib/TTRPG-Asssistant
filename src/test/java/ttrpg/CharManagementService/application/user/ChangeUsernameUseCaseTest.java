package ttrpg.CharManagementService.application.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import ttrpg.CharManagementService.domain.exception.UserAlreadyExistsException;
import ttrpg.CharManagementService.domain.user.User;

class ChangeUsernameUseCaseTest {

    @Test
    void changesUsernameWhenNewValueIsAvailable() {
        var repository = new InMemoryUserRepository();
        var user = User.create("test@example.com", "hero", "hashed:Password1");
        repository.save(user);

        var useCase = new ChangeUsernameUseCase(repository);

        var updatedUser = useCase.execute(new ChangeUsernameCommand(user.getId().value(), "legend"));

        assertEquals("legend", updatedUser.getUsername());
        assertEquals("legend", repository.storage.get(user.getId()).getUsername());
    }

    @Test
    void rejectsTakenUsername() {
        var repository = new InMemoryUserRepository();
        var currentUser = User.create("test@example.com", "hero", "hashed:Password1");
        repository.save(currentUser);
        repository.save(User.create("other@example.com", "legend", "hashed:Password1"));

        var useCase = new ChangeUsernameUseCase(repository);

        assertThrows(
            UserAlreadyExistsException.class,
            () -> useCase.execute(new ChangeUsernameCommand(currentUser.getId().value(), "legend"))
        );
    }

    @Test
    void allowsCaseOnlyUsernameChangeForSameUser() {
        var repository = new InMemoryUserRepository();
        var user = User.create("test@example.com", "hero", "hashed:Password1");
        repository.save(user);

        var useCase = new ChangeUsernameUseCase(repository);

        var updatedUser = useCase.execute(new ChangeUsernameCommand(user.getId().value(), "Hero"));

        assertEquals("Hero", updatedUser.getUsername());
    }

    private static final class InMemoryUserRepository extends ChangePasswordUseCaseTest.InMemoryUserRepository {
        @Override
        public Optional<User> findByUsername(String username) {
            return storage.values().stream()
                .filter(user -> user.getUsername().equalsIgnoreCase(username))
                .findFirst();
        }

        @Override
        public boolean existsByUsername(String username) {
            return storage.values().stream().anyMatch(user -> user.getUsername().equalsIgnoreCase(username));
        }
    }
}
