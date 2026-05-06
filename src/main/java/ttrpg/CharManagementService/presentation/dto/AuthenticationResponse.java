package ttrpg.CharManagementService.presentation.dto;

public record AuthenticationResponse(
    UserResponse user,
    TokenResponse accessToken,
    TokenResponse refreshToken
) {}
