package ttrpg.CharManagementService.infrastructure.redis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration(proxyBeanMethods = false)
public class RedisConfig {

    @Bean
    RedisConnectionFactory redisConnectionFactory(RedisProperties properties) {
        var configuration = new RedisStandaloneConfiguration(properties.host(), properties.port());
        if (properties.password() != null && !properties.password().isBlank()) {
            configuration.setPassword(properties.password());
        }
        return new LettuceConnectionFactory(configuration);
    }

    @Bean
    StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }
}
