package ttrpg.CharManagementService.domain.exception;

public class InvalidCredentialsException extends AuthenticationException {

    public InvalidCredentialsException() {
        this("Invalid credentials");
    }

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
