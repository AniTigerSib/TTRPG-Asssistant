package ttrpg.CharManagementService.domain.exception.ExternalExceptions;

import ttrpg.CharManagementService.domain.exception.ExternalException;

public class ResourceNotFoundException extends ExternalException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
