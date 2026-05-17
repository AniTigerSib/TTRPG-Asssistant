package ttrpg.CharManagementService.domain.exception;

public enum ErrorCode {
    INVALID_INPUT(400, "Invalid input"),
    INVALID_PASSWORD(400, "Invalid password"),
    REQUEST_VALIDATION_FAILED(400, "Request validation failed"),
    MALFORMED_REQUEST_BODY(400, "Malformed request body"),
    INVALID_PARAMETER(400, "Invalid request parameter"),
    UNSUPPORTED_MEDIA_TYPE(415, "Unsupported media type"),
    INVALID_CREDENTIALS(401, "Authentication failed"),
    ACCESS_DENIED(403, "Access denied"),
    RESOURCE_NOT_FOUND(404, "Resource not found"),
    USER_NOT_FOUND(404, "User not found"),
    CAMPAIGN_NOT_FOUND(404, "Campaign not found"),
    CHARACTER_NOT_FOUND(404, "Character not found"),
    GAME_SYSTEM_NOT_FOUND(404, "Game system not found"),
    CHARACTER_TEMPLATE_NOT_FOUND(404, "Character template not found"),
    USER_ALREADY_EXISTS(409, "User already exists"),
    BUSINESS_RULE_VIOLATION(409, "Business rule violation"),
    GAME_SYSTEM_NOT_SUPPORTED(400, "Game system is not supported"),
    INVARIANT_VIOLATION(500, "Internal server error"),
    INTERNAL_SERVER_ERROR(500, "Internal server error");

    private final int httpStatus;
    private final String defaultMessage;

    ErrorCode(int httpStatus, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }

    public int httpStatus() {
        return httpStatus;
    }

    public String defaultMessage() {
        return defaultMessage;
    }
}
