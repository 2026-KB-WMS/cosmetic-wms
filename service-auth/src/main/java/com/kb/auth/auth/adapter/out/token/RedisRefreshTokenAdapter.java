package com.kb.auth.auth.adapter.out.token;

import com.kb.auth.auth.application.port.out.RefreshTokenPort;
import com.kb.auth.global.config.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisRefreshTokenAdapter implements RefreshTokenPort {

    private static final String KEY_PREFIX = "refresh:";

    private final StringRedisTemplate redisTemplate;
    private final JwtProperties jwtProperties;

    @Override
    public void save(Long memberId, String refreshToken) {
        redisTemplate.opsForValue().set(
                KEY_PREFIX + memberId,
                refreshToken,
                jwtProperties.refreshTokenExpirationMs(),
                TimeUnit.MILLISECONDS
        );
    }

    @Override
    public Optional<String> find(Long memberId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(KEY_PREFIX + memberId));
    }

    @Override
    public void delete(Long memberId) {
        redisTemplate.delete(KEY_PREFIX + memberId);
    }
}
