package ttrpg.CharManagementService.presentation.errors;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import ttrpg.CharManagementService.domain.exception.ErrorCode;
import ttrpg.CharManagementService.domain.exception.ServerException;
import ttrpg.CharManagementService.domain.exception.ServiceException;
import ttrpg.CharManagementService.presentation.dto.ApiErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ApiErrorResponse> handleServiceException(
        ServiceException exception,
        HttpServletRequest request
    ) {
        if (exception instanceof ServerException) {
            log.error("Handled server exception [{}] on {} {}", exception.getErrorCode(), request.getMethod(),
                request.getRequestURI(), exception);
        } else {
            log.warn("Handled business exception [{}] on {} {}: {}", exception.getErrorCode(), request.getMethod(),
                request.getRequestURI(), exception.getMessage());
        }

        return build(
            HttpStatusCode.valueOf(exception.getHttpStatus()),
            exception.getErrorCode().name(),
            exception.getPublicMessage(),
            request,
            exception.getDetails()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(
        MethodArgumentNotValidException exception,
        HttpServletRequest request
    ) {
        return build(
            HttpStatusCode.valueOf(ErrorCode.REQUEST_VALIDATION_FAILED.httpStatus()),
            ErrorCode.REQUEST_VALIDATION_FAILED.name(),
            ErrorCode.REQUEST_VALIDATION_FAILED.defaultMessage(),
            request,
            extractFieldErrors(exception.getBindingResult().getFieldErrors())
        );
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiErrorResponse> handleBindException(
        BindException exception,
        HttpServletRequest request
    ) {
        return build(
            HttpStatusCode.valueOf(ErrorCode.REQUEST_VALIDATION_FAILED.httpStatus()),
            ErrorCode.REQUEST_VALIDATION_FAILED.name(),
            ErrorCode.REQUEST_VALIDATION_FAILED.defaultMessage(),
            request,
            extractFieldErrors(exception.getBindingResult().getFieldErrors())
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
        ConstraintViolationException exception,
        HttpServletRequest request
    ) {
        var errors = new LinkedHashMap<String, String>();
        exception.getConstraintViolations().forEach(violation ->
            errors.put(violation.getPropertyPath().toString(), violation.getMessage())
        );

        return build(
            HttpStatusCode.valueOf(ErrorCode.REQUEST_VALIDATION_FAILED.httpStatus()),
            ErrorCode.REQUEST_VALIDATION_FAILED.name(),
            ErrorCode.REQUEST_VALIDATION_FAILED.defaultMessage(),
            request,
            errors
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleMalformedRequest(
        HttpMessageNotReadableException exception,
        HttpServletRequest request
    ) {
        return build(
            HttpStatusCode.valueOf(ErrorCode.MALFORMED_REQUEST_BODY.httpStatus()),
            ErrorCode.MALFORMED_REQUEST_BODY.name(),
            ErrorCode.MALFORMED_REQUEST_BODY.defaultMessage(),
            request,
            Map.of()
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
        MethodArgumentTypeMismatchException exception,
        HttpServletRequest request
    ) {
        var parameterName = exception.getName();
        var message = "Invalid value for parameter: " + parameterName;
        return build(
            HttpStatusCode.valueOf(ErrorCode.INVALID_PARAMETER.httpStatus()),
            ErrorCode.INVALID_PARAMETER.name(),
            message,
            request,
            Map.of(parameterName, message)
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(
        Exception exception,
        HttpServletRequest request
    ) {
        log.error("Unhandled exception on {} {}", request.getMethod(), request.getRequestURI(), exception);
        return build(
            HttpStatusCode.valueOf(ErrorCode.INTERNAL_SERVER_ERROR.httpStatus()),
            ErrorCode.INTERNAL_SERVER_ERROR.name(),
            ErrorCode.INTERNAL_SERVER_ERROR.defaultMessage(),
            request,
            Map.of()
        );
    }

    private ResponseEntity<ApiErrorResponse> build(
        HttpStatusCode status,
        String code,
        String message,
        HttpServletRequest request,
        Map<String, String> details
    ) {
        return ResponseEntity.status(status).body(
            new ApiErrorResponse(
                code,
                message,
                status.value(),
                Instant.now(),
                request.getRequestURI(),
                details
            )
        );
    }

    private Map<String, String> extractFieldErrors(Iterable<FieldError> fieldErrors) {
        var errors = new LinkedHashMap<String, String>();
        for (var fieldError : fieldErrors) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return errors;
    }
}
