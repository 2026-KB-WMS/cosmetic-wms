package com.kb.cosmetic_wms.auth;

import com.kb.cosmetic_wms.auth.adapter.out.redis.RedisRefreshTokenAdapter;
import com.kb.cosmetic_wms.global.config.JwtProperties;
import com.kb.cosmetic_wms.support.RedisTestContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RedisRefreshTokenAdapterTest {

    private StringRedisTemplate redisTemplate;
    private RedisRefreshTokenAdapter adapter;

    @BeforeEach
    void setUp() {
        LettuceConnectionFactory factory = new LettuceConnectionFactory(
                new RedisStandaloneConfiguration(RedisTestContainer.host(), RedisTestContainer.port()));
        factory.afterPropertiesSet();
        redisTemplate = new StringRedisTemplate(factory);
        redisTemplate.afterPropertiesSet();

        JwtProperties properties = new JwtProperties(
                "unit-test-secret-key-which-is-long-enough-0123456789", Duration.ofMinutes(30), Duration.ofDays(14));
        adapter = new RedisRefreshTokenAdapter(redisTemplate, properties);
    }

    @Test
    void 발급한_토큰은_회원_ID와_연결되어_한_번만_소비된다() {
        // given
        String token = adapter.issue(7L);

        // when & then
        assertThat(adapter.consume(token)).contains(7L);
        assertThat(adapter.consume(token)).isEmpty();
    }

    @Test
    void 발급할_때마다_서로_다른_토큰이_만들어진다() {
        assertThat(adapter.issue(7L)).isNotEqualTo(adapter.issue(7L));
    }

    @Test
    void 저장소에는_토큰_원문이_아니라_해시가_저장되고_만료_시간이_설정된다() {
        // given
        String token = adapter.issue(7L);

        // when
        Set<String> keys = redisTemplate.keys("auth:refresh:*");

        // then
        assertThat(keys).isNotEmpty().noneMatch(key -> key.contains(token));
        Long ttl = redisTemplate.getExpire(keys.iterator().next());
        assertThat(ttl).isPositive().isLessThanOrEqualTo(Duration.ofDays(14).toSeconds());
    }

    @Test
    void 폐기한_토큰은_더_이상_소비할_수_없다() {
        // given
        String token = adapter.issue(7L);

        // when
        adapter.revoke(token);

        // then
        assertThat(adapter.consume(token)).isEmpty();
    }

    @Test
    void 알_수_없는_토큰이나_빈_값은_소비되지_않는다() {
        assertThat(adapter.consume("unknown-token")).isEmpty();
        assertThat(adapter.consume("")).isEmpty();
        assertThat(adapter.consume(null)).isEmpty();
    }
}
