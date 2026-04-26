package ttrpg.CharManagementService.domain.exception.InternalExceptions;

import ttrpg.CharManagementService.domain.exception.InternalException;

public class InvalidArgumentException extends InternalException {
    public InvalidArgumentException() {
        super("Invalid method argument");
    }
    public InvalidArgumentException(String message) {
        super(message);
    }
}
