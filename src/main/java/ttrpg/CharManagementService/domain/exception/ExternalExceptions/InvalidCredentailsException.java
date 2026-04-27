package ttrpg.CharManagementService.domain.exception.ExternalExceptions;

public class InvalidCredentailsException extends InvalidDataProvidedException {
    public InvalidCredentailsException() {
        super("Invalid credentails");
    }
    public InvalidCredentailsException(String message) {
        super(message);
    }
}
