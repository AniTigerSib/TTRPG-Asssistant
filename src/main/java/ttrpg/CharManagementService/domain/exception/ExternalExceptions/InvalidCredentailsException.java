package ttrpg.CharManagementService.domain.exception.ExternalExceptions;

import ttrpg.CharManagementService.domain.exception.InvalidOperationException;

public class InvalidCredentailsException extends InvalidOperationException {
    public InvalidCredentailsException() {
        super("Invalid credentails");
    }
    public InvalidCredentailsException(String message) {
        super(message);
    }
}
