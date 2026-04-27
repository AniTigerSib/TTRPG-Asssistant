package ttrpg.CharManagementService.domain.exception.ExternalExceptions;

import ttrpg.CharManagementService.domain.exception.ExternalException;

public class InvalidCredentailsException extends ExternalException {
    public InvalidCredentailsException() {
        super("Invalid credentails");
    }
    public InvalidCredentailsException(String message) {
        super(message);
    }
}
