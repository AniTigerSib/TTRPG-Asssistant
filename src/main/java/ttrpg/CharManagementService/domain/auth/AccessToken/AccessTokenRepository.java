package ttrpg.CharManagementService.domain.auth.AccessToken;

import java.util.Optional;

import ttrpg.CharManagementService.domain.uuid.TokenId;

public interface AccessTokenRepository {
    AccessToken save(AccessToken accessToken);

    Optional<AccessToken> findById(TokenId id);

    void deleteById(TokenId id);
}
