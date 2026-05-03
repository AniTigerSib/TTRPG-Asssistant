package ttrpg.CharManagementService.domain.exception;

public class AuthenticationException extends ClientException {

    public AuthenticationException(String message) {
        super(ErrorCode.INVALID_CREDENTIALS, message);
    }
}
