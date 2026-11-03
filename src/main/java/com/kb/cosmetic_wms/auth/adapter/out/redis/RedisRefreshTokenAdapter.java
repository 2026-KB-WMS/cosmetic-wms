package com.kb.cosmetic_wms.auth.adapter.out.redis;

import com.kb.cosmetic_wms.auth.application.port.out.RefreshTokenPort;
import com.kb.cosmetic_wms.global.config.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RedisRefreshTokenAdapter implements RefreshTokenPort {

    private static final String KEY_PREFIX = "auth:refresh:";
    private static final int TOKEN_BYTES = 32;

    private final StringRedisTemplate redisTemplate;
    private final JwtProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String issue(Long memberId) {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        redisTemplate.opsForValue().set(key(token), String.valueOf(memberId), properties.refreshTokenTtl());
        return token;
    }

    @Override
    public Optional<Long> consume(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(redisTemplate.opsForValue().getAndDelete(key(refreshToken)))
                .map(Long::valueOf);
    }

    @Override
    public void revoke(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }
        redisTemplate.delete(key(refreshToken));
    }

    private String key(String token) {
        return KEY_PREFIX + sha256(token);
    }

    private static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256을 사용할 수 없습니다.", e);
        }
    }
}
