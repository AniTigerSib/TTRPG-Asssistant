package ttrpg.CharManagementService.infrastructure.security;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import ttrpg.CharManagementService.domain.exception.ErrorCode;
import ttrpg.CharManagementService.presentation.dto.ApiErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class ApiAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException authException
    ) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(
            response.getOutputStream(),
            new ApiErrorResponse(
                ErrorCode.INVALID_CREDENTIALS.name(),
                ErrorCode.INVALID_CREDENTIALS.defaultMessage(),
                HttpStatus.UNAUTHORIZED.value(),
                Instant.now(),
                request.getRequestURI(),
                Map.of()
            )
        );
    }
}
