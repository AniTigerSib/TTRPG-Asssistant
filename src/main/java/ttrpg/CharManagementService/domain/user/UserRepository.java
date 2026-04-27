package ttrpg.CharManagementService.domain.user;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(UserId id);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    User save(User user);

    void delete(User user);

    boolean existsById(UserId id);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
}
