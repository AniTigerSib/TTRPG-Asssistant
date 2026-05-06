package ttrpg.CharManagementService.application.auth;

import ttrpg.CharManagementService.domain.user.User;

public record AuthenticationResult(
    User user,
    AuthenticationTokens tokens
) {}
