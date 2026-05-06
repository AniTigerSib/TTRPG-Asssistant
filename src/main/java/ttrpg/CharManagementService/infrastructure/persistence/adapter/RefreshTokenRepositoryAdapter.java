package ttrpg.CharManagementService.infrastructure.persistence.adapter;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import ttrpg.CharManagementService.domain.auth.RefreshToken.RefreshToken;
import ttrpg.CharManagementService.domain.auth.RefreshToken.RefreshTokenId;
import ttrpg.CharManagementService.domain.auth.RefreshToken.RefreshTokenRepository;
import ttrpg.CharManagementService.infrastructure.persistence.mapper.RefreshTokenPersistenceMapper;
import ttrpg.CharManagementService.infrastructure.persistence.repository.RefreshTokenJpaRepository;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    private final RefreshTokenJpaRepository jpaRepository;
    private final RefreshTokenPersistenceMapper mapper;

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        var saved = jpaRepository.save(mapper.toEntity(refreshToken));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<RefreshToken> findById(RefreshTokenId id) {
        return jpaRepository.findById(id.value())
            .map(mapper::toDomain);
    }
}
