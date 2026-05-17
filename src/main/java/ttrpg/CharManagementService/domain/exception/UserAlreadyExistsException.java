package ttrpg.CharManagementService.domain.exception;

import java.util.Map;

public class UserAlreadyExistsException extends ClientException {

    private UserAlreadyExistsException(String message, Map<String, String> details) {
        super(ErrorCode.USER_ALREADY_EXISTS, message, details);
    }

    public static UserAlreadyExistsException email(String email) {
        var message = "Email is already registered";
        return new UserAlreadyExistsException(message, Map.of("email", message));
    }

    public static UserAlreadyExistsException username(String username) {
        var message = "Username is already taken";
        return new UserAlreadyExistsException(message, Map.of("username", message));
    }
}
