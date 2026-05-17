package ttrpg.CharManagementService.domain.exception;

import java.util.Map;

public class ValidationException extends ClientException {

    public ValidationException(String message) {
        super(ErrorCode.INVALID_INPUT, message);
    }

    public ValidationException(String message, Map<String, String> details) {
        super(ErrorCode.INVALID_INPUT, message, details);
    }
}
