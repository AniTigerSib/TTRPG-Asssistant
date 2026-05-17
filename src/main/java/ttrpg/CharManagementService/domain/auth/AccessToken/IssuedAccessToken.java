package ttrpg.CharManagementService.domain.auth.AccessToken;

import ttrpg.CharManagementService.domain.shared.Checkers;

public record IssuedAccessToken(
    String rawToken,
    AccessToken accessToken
) {
    public IssuedAccessToken {
        Checkers.requireStringNonBlank(rawToken, "rawToken");
        Checkers.requireNonNull(accessToken, "accessToken");
    }
}
