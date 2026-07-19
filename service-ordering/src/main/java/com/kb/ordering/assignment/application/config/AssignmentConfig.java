package com.kb.ordering.assignment.application.config;

import com.kb.ordering.assignment.domain.service.WeightBasedAssignmentPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AssignmentConfig {

    @Bean
    public WeightBasedAssignmentPolicy weightBasedAssignmentPolicy() {
        return WeightBasedAssignmentPolicy.withDefaults();
    }
}
