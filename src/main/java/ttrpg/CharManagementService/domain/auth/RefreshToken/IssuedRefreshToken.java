package ttrpg.CharManagementService.domain.auth.RefreshToken;

import ttrpg.CharManagementService.domain.shared.Checkers;

public record IssuedRefreshToken(
    String rawToken,
    RefreshToken refreshToken
) {
    public IssuedRefreshToken {
        Checkers.requireStringNonBlank(rawToken, "rawToken");
        Checkers.requireNonNull(refreshToken, "refreshToken");
    }
}
