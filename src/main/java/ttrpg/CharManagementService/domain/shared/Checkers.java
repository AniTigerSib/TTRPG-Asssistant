package ttrpg.CharManagementService.domain.shared;

import ttrpg.CharManagementService.domain.exception.InvalidInputException;
import ttrpg.CharManagementService.domain.exception.InvariantViolationException;

public class Checkers {

    public static <T> T requireNonNull(T object, String objectName) {
        if (object == null) {
            throw new InvariantViolationException(objectName + " must not be null");
        }
        return object;
    }

    public static String requireStringNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw InvalidInputException.blankField(fieldName);
        }
        return value;
    }
}
