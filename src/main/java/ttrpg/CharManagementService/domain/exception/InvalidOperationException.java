package ttrpg.CharManagementService.domain.exception;

public class InvalidOperationException extends ServiceException {
    public InvalidOperationException() {
        super("Invalid operation");
    }
    public InvalidOperationException(String message) {
        super(message);
    }
}
