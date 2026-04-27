package ttrpg.CharManagementService.presentation.dto;

import java.time.Instant;
import java.util.Map;

public record ApiErrorResponse(
    String code,
    String message,
    int status,
    Instant timestamp,
    String path,
    Map<String, String> errors
) {}
