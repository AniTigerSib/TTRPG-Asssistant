package ttrpg.CharManagementService.domain.shared;

import ttrpg.CharManagementService.domain.exception.ExternalExceptions.InvalidDataProvidedException;
import ttrpg.CharManagementService.domain.exception.InternalExceptions.InvalidArgumentException;

public class Checkers {

    public static <T> T requireNonNull(T object, String objectName) {
        if (object == null) {
            throw new InvalidArgumentException(objectName + " must not be null");
        }
        return object;
    }

    public static String requireStringNonBlank(String value, String fieldName) {
        requireNonNull(value, fieldName);
        if (value.isBlank()) {
            throw new InvalidDataProvidedException(fieldName + " must not be blank");
        }
        return value;
    }
}
