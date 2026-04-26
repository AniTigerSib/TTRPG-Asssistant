package ttrpg.CharManagementService.domain.exception;

public class ServiceException extends RuntimeException {
    public ServiceException() {
        super("Service error");
    }
    public ServiceException(String message) {
        super(message);
    }
}
