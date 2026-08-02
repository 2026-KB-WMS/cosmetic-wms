package com.kb.auth.global.config;

import com.kb.common.security.JwtAuthenticationFilter;
import com.kb.common.security.JwtClaimExtractor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "jwt.filter.enabled", havingValue = "true")
public class JwtFilterConfig {

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtClaimExtractor claimExtractor) {
        return new JwtAuthenticationFilter(claimExtractor);
    }
}
