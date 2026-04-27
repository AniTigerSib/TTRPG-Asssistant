package ttrpg.CharManagementService.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import ttrpg.CharManagementService.domain.user.User;
import ttrpg.CharManagementService.domain.user.UserId;
import ttrpg.CharManagementService.domain.user.UserSnapshot;
import ttrpg.CharManagementService.infrastructure.persistence.entity.UserJpaEntity;

@Component
public class UserPersistenceMapper {
    public User toDomain(UserJpaEntity userJpaEntity) {
        return User.restore(new UserSnapshot(
            new UserId(userJpaEntity.getId()),
            userJpaEntity.getEmail(),
            userJpaEntity.getUsername(),
            userJpaEntity.getPasswordHash(),
            userJpaEntity.getRoles(),
            userJpaEntity.getCreatedAt(),
            userJpaEntity.getUpdatedAt()
        ));
    }

    public UserJpaEntity toEntity(User user) {
        var snapshot = user.snapshot();

        return UserJpaEntity.builder()
            .id(snapshot.id().value())
            .email(snapshot.email())
            .username(snapshot.username())
            .passwordHash(snapshot.passwordHash())
            .roles(snapshot.roles())
            .createdAt(snapshot.createdAt())
            .updatedAt(snapshot.updatedAt())
            .build();
    }
}
