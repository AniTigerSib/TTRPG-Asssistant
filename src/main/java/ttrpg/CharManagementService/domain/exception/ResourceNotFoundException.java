package ttrpg.CharManagementService.domain.exception;

public class ResourceNotFoundException extends ClientException {

    public ResourceNotFoundException(String message) {
        this(ErrorCode.RESOURCE_NOT_FOUND, message);
    }

    protected ResourceNotFoundException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
