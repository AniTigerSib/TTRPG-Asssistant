package ttrpg.CharManagementService.domain.auth.RefreshToken;

import java.util.Optional;

public interface RefreshTokenRepository {
    RefreshToken save(RefreshToken refreshToken);

    Optional<RefreshToken> findById(RefreshTokenId id);
}
