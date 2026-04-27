package ttrpg.CharManagementService.domain.exception.ExternalExceptions;

import ttrpg.CharManagementService.domain.exception.ExternalException;

public class InvalidDataProvided extends ExternalException {
    public InvalidDataProvided() {
        super("Invalid data provided");
    }
    public InvalidDataProvided(String message) {
        super(message);
    }
}
