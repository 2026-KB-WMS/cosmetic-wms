package com.kb.cosmetic_wms.support;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * JVM당 하나만 띄우는 Redis 컨테이너.
 */
public final class RedisTestContainer {

    private static final int PORT = 6379;

    private static final GenericContainer<?> REDIS =
            new GenericContainer<>(DockerImageName.parse("redis:7.4")).withExposedPorts(PORT);

    static {
        REDIS.start();
    }

    private RedisTestContainer() {
    }

    public static String host() {
        return REDIS.getHost();
    }

    public static int port() {
        return REDIS.getMappedPort(PORT);
    }
}
