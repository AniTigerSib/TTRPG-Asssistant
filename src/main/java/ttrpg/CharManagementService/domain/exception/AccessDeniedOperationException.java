package ttrpg.CharManagementService.domain.exception;

public class AccessDeniedOperationException extends ClientException {

    public AccessDeniedOperationException(String message) {
        super(ErrorCode.ACCESS_DENIED, message);
    }
}
