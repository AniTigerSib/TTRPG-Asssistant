package ttrpg.CharManagementService.domain.exception.ExternalExceptions;

public class InvalidPasswordException extends InvalidDataProvidedException {
    public InvalidPasswordException(String message) {
        super(message);
    }
}
