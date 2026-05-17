package ttrpg.CharManagementService.infrastructure.redis.adapter;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import ttrpg.CharManagementService.domain.auth.AccessToken.AccessToken;
import ttrpg.CharManagementService.domain.auth.AccessToken.AccessTokenRepository;
import ttrpg.CharManagementService.domain.auth.AccessToken.AccessTokenSnapshot;
import ttrpg.CharManagementService.domain.user.UserId;
import ttrpg.CharManagementService.domain.uuid.TokenId;

@Repository
@RequiredArgsConstructor
public class AccessTokenRedisRepositoryAdapter implements AccessTokenRepository {
    private static final String KEY_PREFIX = "auth:access-token:";
    private static final String USER_ID_FIELD = "userId";
    private static final String TOKEN_HASH_FIELD = "tokenHash";
    private static final String EXPIRES_AT_FIELD = "expiresAt";
    private static final String CREATED_AT_FIELD = "createdAt";

    private final StringRedisTemplate redisTemplate;

    @Override
    public AccessToken save(AccessToken accessToken) {
        var snapshot = accessToken.snapshot();
        var key = key(snapshot.id());

        redisTemplate.opsForHash().put(key, USER_ID_FIELD, snapshot.userId().value().toString());
        redisTemplate.opsForHash().put(key, TOKEN_HASH_FIELD, snapshot.tokenHash());
        redisTemplate.opsForHash().put(key, EXPIRES_AT_FIELD, snapshot.expiresAt().toString());
        redisTemplate.opsForHash().put(key, CREATED_AT_FIELD, snapshot.createdAt().toString());

        var ttl = Duration.between(Instant.now(), snapshot.expiresAt());
        redisTemplate.expire(key, ttl.isNegative() ? Duration.ZERO : ttl);

        return accessToken;
    }

    @Override
    public Optional<AccessToken> findById(TokenId id) {
        var entries = redisTemplate.opsForHash().entries(key(id));
        if (entries.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(AccessToken.restore(
            new AccessTokenSnapshot(
                id,
                new UserId(java.util.UUID.fromString((String) entries.get(USER_ID_FIELD))),
                (String) entries.get(TOKEN_HASH_FIELD),
                Instant.parse((String) entries.get(EXPIRES_AT_FIELD)),
                Instant.parse((String) entries.get(CREATED_AT_FIELD))
            )
        ));
    }

    @Override
    public void deleteById(TokenId id) {
        redisTemplate.delete(key(id));
    }

    private String key(TokenId id) {
        return KEY_PREFIX + id.value();
    }
}
