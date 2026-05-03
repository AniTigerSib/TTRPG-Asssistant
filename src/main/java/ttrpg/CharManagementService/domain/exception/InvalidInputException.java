package ttrpg.CharManagementService.domain.exception;

import java.util.Map;

public class InvalidInputException extends ValidationException {

    private InvalidInputException(String message, Map<String, String> details) {
        super(message, details);
    }

    public static InvalidInputException blankField(String fieldName) {
        var message = fieldName + " must not be blank";
        return new InvalidInputException(message, Map.of(fieldName, message));
    }

    public static InvalidInputException nullField(String fieldName) {
        var message = fieldName + " must not be null";
        return new InvalidInputException(message, Map.of(fieldName, message));
    }

    public static InvalidInputException invalidValue(String fieldName, String message) {
        return new InvalidInputException(message, Map.of(fieldName, message));
    }
}
