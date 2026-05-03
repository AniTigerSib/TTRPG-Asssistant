package ttrpg.CharManagementService.domain.exception;

import java.util.Map;

public abstract class ServerException extends ServiceException {

    protected ServerException(ErrorCode errorCode, String internalMessage) {
        this(errorCode, internalMessage, null);
    }

    protected ServerException(ErrorCode errorCode, String internalMessage, Throwable cause) {
        super(errorCode, errorCode.defaultMessage(), internalMessage, cause, Map.of());
    }
}
