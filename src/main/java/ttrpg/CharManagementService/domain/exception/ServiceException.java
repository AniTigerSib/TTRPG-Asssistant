package ttrpg.CharManagementService.domain.exception;

import java.util.Map;

public abstract class ServiceException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String publicMessage;
    private final Map<String, String> details;

    protected ServiceException(
        ErrorCode errorCode,
        String publicMessage,
        String internalMessage,
        Throwable cause,
        Map<String, String> details
    ) {
        super(internalMessage, cause);
        this.errorCode = errorCode;
        this.publicMessage = publicMessage;
        this.details = Map.copyOf(details);
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public int getHttpStatus() {
        return errorCode.httpStatus();
    }

    public String getPublicMessage() {
        return publicMessage;
    }

    public Map<String, String> getDetails() {
        return details;
    }
}
