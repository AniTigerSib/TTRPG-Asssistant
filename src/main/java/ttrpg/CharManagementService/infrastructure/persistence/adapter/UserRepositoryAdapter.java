package ttrpg.CharManagementService.infrastructure.persistence.adapter;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import ttrpg.CharManagementService.domain.user.User;
import ttrpg.CharManagementService.domain.user.UserId;
import ttrpg.CharManagementService.domain.user.UserRepository;
import ttrpg.CharManagementService.infrastructure.persistence.mapper.UserPersistenceMapper;
import ttrpg.CharManagementService.infrastructure.persistence.repository.UserJpaRepository;

@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository jpaRepository;
    private final UserPersistenceMapper mapper;

    @Override
    public Optional<User> findById(UserId id) {
        return jpaRepository.findById(id.value())
                .map(mapper::toDomain);
    }
    @Override
    public User save(User user) {
        var entity = mapper.toEntity(user);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
    @Override
    public void delete(User user) {
        jpaRepository.deleteById(user.getId().value());
    }
    @Override
    public boolean existsById(UserId id) {
        return jpaRepository.existsById(id.value());
    }
}
