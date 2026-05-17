package ttrpg.CharManagementService.domain.exception;

import java.util.Map;

public class ConflictException extends ClientException {

    public ConflictException(String message) {
        this(message, Map.of());
    }

    public ConflictException(String message, Map<String, String> details) {
        super(ErrorCode.BUSINESS_RULE_VIOLATION, message, details);
    }
}
