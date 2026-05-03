package ttrpg.CharManagementService.domain.exception;

public class InvariantViolationException extends ServerException {

    public InvariantViolationException(String message) {
        super(ErrorCode.INVARIANT_VIOLATION, message);
    }

    public InvariantViolationException(String message, Throwable cause) {
        super(ErrorCode.INVARIANT_VIOLATION, message, cause);
    }
}
