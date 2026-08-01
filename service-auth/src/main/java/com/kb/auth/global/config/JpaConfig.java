package com.kb.auth.global.config;

import com.kb.auth.global.common.AuditorAwareImpl;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@EnableConfigurationProperties(JwtProperties.class)
public class JpaConfig {

    @Bean
    public AuditorAware<Long> auditorProvider() {
        return new AuditorAwareImpl();
    }
}