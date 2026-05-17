package ttrpg.CharManagementService.domain.exception;

import java.util.UUID;

public class UserNotFoundException extends ResourceNotFoundException {

    public UserNotFoundException(UUID userId) {
        super(ErrorCode.USER_NOT_FOUND, "User not found: " + userId);
    }
}
