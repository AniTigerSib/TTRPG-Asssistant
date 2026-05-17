package ttrpg.CharManagementService.infrastructure.persistence.mapper;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.springframework.stereotype.Component;

import ttrpg.CharManagementService.domain.auth.RefreshToken.RefreshToken;
import ttrpg.CharManagementService.domain.auth.RefreshToken.RefreshTokenId;
import ttrpg.CharManagementService.domain.auth.RefreshToken.RefreshTokenSnapshot;
import ttrpg.CharManagementService.domain.exception.InvariantViolationException;
import ttrpg.CharManagementService.domain.user.UserId;
import ttrpg.CharManagementService.infrastructure.persistence.entity.RefreshTokenJpaEntity;

@Component
public class RefreshTokenPersistenceMapper {

    public RefreshToken toDomain(RefreshTokenJpaEntity entity) {
        return RefreshToken.restore(
            new RefreshTokenSnapshot(
                new RefreshTokenId(entity.getId()),
                new UserId(entity.getUserId()),
                entity.getTokenHash(),
                entity.getUserAgent(),
                parseIpAddress(entity.getIpAddress()),
                entity.getExpiresAt(),
                entity.getRevokedAt(),
                entity.getCreatedAt()
            )
        );
    }

    public RefreshTokenJpaEntity toEntity(RefreshToken refreshToken) {
        var snapshot = refreshToken.snapshot();
        return RefreshTokenJpaEntity.builder()
            .id(snapshot.id().value())
            .userId(snapshot.userId().value())
            .tokenHash(snapshot.tokenHash())
            .userAgent(snapshot.userAgent())
            .ipAddress(snapshot.ipAddress().getHostAddress())
            .expiresAt(snapshot.expiresAt())
            .revokedAt(snapshot.revokedAt())
            .createdAt(snapshot.createdAt())
            .build();
    }

    private InetAddress parseIpAddress(String value) {
        try {
            return InetAddress.getByName(value);
        } catch (UnknownHostException exception) {
            throw new InvariantViolationException("Invalid persisted IP address: " + value, exception);
        }
    }
}
