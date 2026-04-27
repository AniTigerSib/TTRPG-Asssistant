package ttrpg.CharManagementService.domain.exception;

public class ExternalException extends ServiceException {
    public ExternalException() {
        super("Invalid data or operation");
    }
    public ExternalException(String message) {
        super(message);
    }
}
