package com.kb.auth.global.config;

import com.kb.common.security.JwtAuthenticationFilter;
import com.kb.common.security.JwtClaimExtractor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class JwtFilterConfig {

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtClaimExtractor claimExtractor) {
        return new JwtAuthenticationFilter(claimExtractor);
    }
}
