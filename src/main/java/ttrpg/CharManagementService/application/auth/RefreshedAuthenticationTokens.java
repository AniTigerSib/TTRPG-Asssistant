package ttrpg.CharManagementService.application.auth;

import ttrpg.CharManagementService.domain.user.UserId;

public record RefreshedAuthenticationTokens(
    UserId userId,
    AuthenticationTokens tokens
) {}
