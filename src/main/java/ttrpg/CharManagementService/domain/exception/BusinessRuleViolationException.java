package ttrpg.CharManagementService.domain.exception;

import java.util.Map;

public class BusinessRuleViolationException extends ConflictException {

    public BusinessRuleViolationException(String message) {
        this(message, Map.of());
    }

    public BusinessRuleViolationException(String message, Map<String, String> details) {
        super(message, details);
    }
}
