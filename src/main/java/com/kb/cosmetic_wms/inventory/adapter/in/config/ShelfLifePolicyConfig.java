package com.kb.cosmetic_wms.inventory.adapter.in.config;

import com.kb.cosmetic_wms.inventory.domain.model.ShelfLifePolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ShelfLifePolicyConfig {

    @Bean
    public ShelfLifePolicy shelfLifePolicy(@Value("${inventory.shelf-life.min-remaining-days}") int minRemainingDays) {
        return new ShelfLifePolicy(minRemainingDays);
    }
}
