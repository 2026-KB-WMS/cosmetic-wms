package com.kb.cosmetic_wms.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * MySQL과 Redis가 모두 필요한 테스트의 베이스.
 */
public abstract class IntegrationTestSupport extends MySqlContainerSupport {

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", RedisTestContainer::host);
        registry.add("spring.data.redis.port", RedisTestContainer::port);
    }
}
