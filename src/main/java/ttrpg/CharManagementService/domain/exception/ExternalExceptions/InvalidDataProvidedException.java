package ttrpg.CharManagementService.domain.exception.ExternalExceptions;

import ttrpg.CharManagementService.domain.exception.ExternalException;

public class InvalidDataProvidedException extends ExternalException {
    public InvalidDataProvidedException() {
        super("Invalid data provided");
    }
    public InvalidDataProvidedException(String message) {
        super(message);
    }
}
