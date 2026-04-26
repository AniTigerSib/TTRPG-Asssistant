package ttrpg.CharManagementService.domain.exception;

public class InternalException extends ServiceException {
    public InternalException() {
        super("Internal server error");
    }
    public InternalException(String message) {
        super(message);
    }
}
