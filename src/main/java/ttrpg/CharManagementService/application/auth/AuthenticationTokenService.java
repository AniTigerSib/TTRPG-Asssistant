package ttrpg.CharManagementService.application.auth;

import java.net.InetAddress;
import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ttrpg.CharManagementService.domain.auth.AccessToken.AccessToken;
import ttrpg.CharManagementService.domain.auth.AccessToken.AccessTokenRepository;
import ttrpg.CharManagementService.domain.auth.RefreshToken.RefreshToken;
import ttrpg.CharManagementService.domain.auth.RefreshToken.RefreshTokenRepository;
import ttrpg.CharManagementService.domain.exception.InvalidTokenException;
import ttrpg.CharManagementService.domain.shared.Checkers;
import ttrpg.CharManagementService.domain.user.UserId;
import ttrpg.CharManagementService.infrastructure.security.config.AuthTokenProperties;

@Service
@RequiredArgsConstructor
public class AuthenticationTokenService {

    private final AccessTokenRepository accessTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthTokenProperties authTokenProperties;

    public AuthenticationTokens issueTokens(UserId userId, String userAgent, InetAddress ipAddress) {
        Checkers.requireNonNull(userId, "userId");
        Checkers.requireStringNonBlank(userAgent, "userAgent");
        Checkers.requireNonNull(ipAddress, "ipAddress");

        var accessExpiresAt = Instant.now().plus(authTokenProperties.accessTtl());
        var issuedAccessToken = AccessToken.issue(userId, accessExpiresAt);
        accessTokenRepository.save(issuedAccessToken.accessToken());

        var refreshExpiresAt = Instant.now().plus(authTokenProperties.refreshTtl());
        var issuedRefreshToken = RefreshToken.issue(userId, userAgent, ipAddress, refreshExpiresAt);
        refreshTokenRepository.save(issuedRefreshToken.refreshToken());

        return new AuthenticationTokens(
            issuedAccessToken.rawToken(),
            issuedAccessToken.accessToken().expiresAt(),
            issuedRefreshToken.rawToken(),
            issuedRefreshToken.refreshToken().getExpiresAt()
        );
    }

    public RefreshedAuthenticationTokens refreshTokens(String rawRefreshToken, String userAgent, InetAddress ipAddress) {
        Checkers.requireStringNonBlank(rawRefreshToken, "refreshToken");
        Checkers.requireStringNonBlank(userAgent, "userAgent");
        Checkers.requireNonNull(ipAddress, "ipAddress");

        var refreshTokenId = RefreshToken.extractTokenId(rawRefreshToken);
        var storedRefreshToken = refreshTokenRepository.findById(refreshTokenId)
            .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (!storedRefreshToken.matches(rawRefreshToken)) {
            throw new InvalidTokenException("Invalid refresh token");
        }
        if (!storedRefreshToken.isValid()) {
            refreshTokenRepository.save(storedRefreshToken);
            throw new InvalidTokenException("Refresh token is expired or revoked");
        }

        storedRefreshToken.revoke();
        refreshTokenRepository.save(storedRefreshToken);

        var tokens = issueTokens(storedRefreshToken.getUserId(), userAgent, ipAddress);
        return new RefreshedAuthenticationTokens(storedRefreshToken.getUserId(), tokens);
    }

    public Optional<UserId> resolveUserIdByAccessToken(String rawAccessToken) {
        Checkers.requireStringNonBlank(rawAccessToken, "accessToken");

        var accessTokenId = AccessToken.extractTokenId(rawAccessToken);
        var storedAccessToken = accessTokenRepository.findById(accessTokenId);
        if (storedAccessToken.isEmpty()) {
            return Optional.empty();
        }

        var accessToken = storedAccessToken.get();
        if (!accessToken.matches(rawAccessToken)) {
            return Optional.empty();
        }
        if (accessToken.isExpired()) {
            accessTokenRepository.deleteById(accessTokenId);
            return Optional.empty();
        }

        return Optional.of(accessToken.userId());
    }
}
