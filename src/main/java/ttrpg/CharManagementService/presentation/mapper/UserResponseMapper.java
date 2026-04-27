package ttrpg.CharManagementService.presentation.mapper;

import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import ttrpg.CharManagementService.domain.user.User;
import ttrpg.CharManagementService.presentation.dto.UserResponse;

@Component
public class UserResponseMapper {
    public UserResponse toUserResponse(User user) {
        var snapshot = user.snapshot();
        return new UserResponse(
            snapshot.id().value(),
            snapshot.email(),
            snapshot.username(),
            snapshot.roles().stream().map(Enum::name).collect(Collectors.toUnmodifiableSet()),
            snapshot.createdAt(),
            snapshot.updatedAt()
        );
    }
}
