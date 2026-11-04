package com.kb.cosmetic_wms.inventory;

import com.kb.cosmetic_wms.inventory.adapter.in.config.ShelfLifePolicyConfig;
import com.kb.cosmetic_wms.inventory.domain.model.ShelfLifePolicy;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.SystemEnvironmentPropertySource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ShelfLifePolicyConfigTest {

    private ShelfLifePolicy policyFrom(org.springframework.core.env.PropertySource<?> source) {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment().getPropertySources().addFirst(source);
            context.register(ShelfLifePolicyConfig.class);
            context.refresh();
            return context.getBean(ShelfLifePolicy.class);
        }
    }

    @Test
    void 설정_값으로_최소_잔여일_정책을_만든다() {
        ShelfLifePolicy policy = policyFrom(new MapPropertySource("test",
                Map.of("inventory.shelf-life.min-remaining-days", "14")));

        assertThat(policy.minRemainingDays()).isEqualTo(14);
    }

    @Test
    void 환경변수로도_최소_잔여일을_바꿀_수_있다() {
        ShelfLifePolicy policy = policyFrom(new SystemEnvironmentPropertySource("env",
                Map.of("INVENTORY_SHELF_LIFE_MIN_REMAINING_DAYS", "3")));

        assertThat(policy.minRemainingDays()).isEqualTo(3);
    }
}
