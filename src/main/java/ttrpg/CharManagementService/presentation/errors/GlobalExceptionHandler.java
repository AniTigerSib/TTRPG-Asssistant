package ttrpg.CharManagementService.presentation.errors;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
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
import ttrpg.CharManagementService.domain.exception.ExternalException;
import ttrpg.CharManagementService.domain.exception.InternalException;
import ttrpg.CharManagementService.domain.exception.ExternalExceptions.ResourceNotFoundException;
import ttrpg.CharManagementService.presentation.dto.ApiErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
        ResourceNotFoundException exception,
        HttpServletRequest request
    ) {
        return build(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(ExternalException.class)
    public ResponseEntity<ApiErrorResponse> handleExternal(
        ExternalException exception,
        HttpServletRequest request
    ) {
        return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(InternalException.class)
    public ResponseEntity<ApiErrorResponse> handleInternal(
        InternalException exception,
        HttpServletRequest request
    ) {
        return build(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "INTERNAL_SERVER_ERROR",
            "Internal server error",
            request,
            Map.of()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(
        MethodArgumentNotValidException exception,
        HttpServletRequest request
    ) {
        return build(
            HttpStatus.BAD_REQUEST,
            "REQUEST_VALIDATION_FAILED",
            "Request validation failed",
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
            HttpStatus.BAD_REQUEST,
            "REQUEST_VALIDATION_FAILED",
            "Request validation failed",
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
            HttpStatus.BAD_REQUEST,
            "REQUEST_VALIDATION_FAILED",
            "Request validation failed",
            request,
            errors
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleMalformedRequest(
        HttpMessageNotReadableException exception,
        HttpServletRequest request
    ) {
        return build(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST_BODY", "Malformed request body", request, Map.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
        MethodArgumentTypeMismatchException exception,
        HttpServletRequest request
    ) {
        var parameterName = exception.getName();
        return build(
            HttpStatus.BAD_REQUEST,
            "INVALID_PARAMETER",
            "Invalid value for parameter: " + parameterName,
            request,
            Map.of(parameterName, "Invalid value")
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(
        Exception exception,
        HttpServletRequest request
    ) {
        return build(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "INTERNAL_SERVER_ERROR",
            "Internal server error",
            request,
            Map.of()
        );
    }

    private ResponseEntity<ApiErrorResponse> build(
        HttpStatus status,
        String code,
        String message,
        HttpServletRequest request,
        Map<String, String> errors
    ) {
        return ResponseEntity.status(status).body(
            new ApiErrorResponse(
                code,
                message,
                status.value(),
                Instant.now(),
                request.getRequestURI(),
                errors
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
