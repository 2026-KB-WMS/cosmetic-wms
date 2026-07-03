package com.kb.cosmetic_wms.oms.application.config;

import com.kb.cosmetic_wms.oms.domain.service.WeightBasedAssignmentPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OmsConfig {

    @Bean
    public WeightBasedAssignmentPolicy weightBasedAssignmentPolicy() {
        return WeightBasedAssignmentPolicy.withDefaults();
    }
}
