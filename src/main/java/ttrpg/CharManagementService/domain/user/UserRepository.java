package ttrpg.CharManagementService.domain.user;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(UserId id);

    User save(User user);

    void delete(User user);

    boolean existsById(UserId id);
}
