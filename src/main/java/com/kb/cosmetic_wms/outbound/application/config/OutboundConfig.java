package com.kb.cosmetic_wms.outbound.application.config;

import com.kb.cosmetic_wms.outbound.domain.service.FefoAllocationSelector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OutboundConfig {

    @Bean
    public FefoAllocationSelector fefoAllocationSelector() {
        return new FefoAllocationSelector();
    }
}
