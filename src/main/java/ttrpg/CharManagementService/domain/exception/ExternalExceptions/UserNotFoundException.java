package ttrpg.CharManagementService.domain.exception.ExternalExceptions;

import java.util.UUID;

public class UserNotFoundException extends ResourceNotFoundException {
    public UserNotFoundException(UUID userId) {
        super("User not found: " + userId);
    }
}
