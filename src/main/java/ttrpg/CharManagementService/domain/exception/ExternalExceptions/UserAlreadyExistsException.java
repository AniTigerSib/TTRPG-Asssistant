package ttrpg.CharManagementService.domain.exception.ExternalExceptions;

public class UserAlreadyExistsException extends InvalidDataProvidedException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
