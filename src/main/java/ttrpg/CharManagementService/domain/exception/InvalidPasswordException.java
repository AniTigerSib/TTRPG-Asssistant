package ttrpg.CharManagementService.domain.exception;

import java.util.Map;

public class InvalidPasswordException extends ClientException {

    public InvalidPasswordException(String message) {
        this(message, Map.of("password", message));
    }

    public InvalidPasswordException(String message, String fieldName) {
        this(message, Map.of(fieldName, message));
    }

    private InvalidPasswordException(String message, Map<String, String> details) {
        super(ErrorCode.INVALID_PASSWORD, message, details);
    }
}
