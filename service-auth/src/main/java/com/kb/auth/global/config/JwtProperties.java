package com.kb.auth.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String privateKeyPath,
        long accessTokenExpirationMs,
        long refreshTokenExpirationMs,
        String issuer,
        String kid
) {
}