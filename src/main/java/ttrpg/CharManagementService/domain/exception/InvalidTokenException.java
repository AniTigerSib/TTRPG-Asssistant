package ttrpg.CharManagementService.domain.exception;

public class InvalidTokenException extends AuthenticationException {

    public InvalidTokenException() {
        this("Invalid token");
    }

    public InvalidTokenException(String message) {
        super(message);
    }
}
