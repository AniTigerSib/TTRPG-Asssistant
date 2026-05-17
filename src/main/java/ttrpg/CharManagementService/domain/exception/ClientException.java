package ttrpg.CharManagementService.domain.exception;

import java.util.Map;

public abstract class ClientException extends ServiceException {

    protected ClientException(ErrorCode errorCode, String message) {
        this(errorCode, message, Map.of());
    }

    protected ClientException(ErrorCode errorCode, String message, Map<String, String> details) {
        super(errorCode, message, message, null, details);
    }
}
