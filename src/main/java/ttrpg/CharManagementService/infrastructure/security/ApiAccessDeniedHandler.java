package ttrpg.CharManagementService.infrastructure.security;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import ttrpg.CharManagementService.domain.exception.ErrorCode;
import ttrpg.CharManagementService.presentation.dto.ApiErrorResponse;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class ApiAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(
        HttpServletRequest request,
        HttpServletResponse response,
        AccessDeniedException accessDeniedException
    ) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(
            response.getOutputStream(),
            new ApiErrorResponse(
                ErrorCode.ACCESS_DENIED.name(),
                ErrorCode.ACCESS_DENIED.defaultMessage(),
                HttpStatus.FORBIDDEN.value(),
                Instant.now(),
                request.getRequestURI(),
                Map.of()
            )
        );
    }
}
