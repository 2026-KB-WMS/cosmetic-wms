package com.kb.cosmetic_wms.architecture;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class DependencyRulesTest {

    private static final String ROOT = "com.kb.cosmetic_wms.";

    @Test
    void 루트_직속이나_global은_업무_도메인이_아니다() {
        assertThat(DependencyRules.businessDomainOf("com.kb.cosmetic_wms")).isNull();
        assertThat(DependencyRules.businessDomainOf(ROOT + "global.error")).isNull();
        assertThat(DependencyRules.businessDomainOf("java.util")).isNull();
    }

    @Test
    void 업무_도메인은_루트_바로_아래_패키지다() {
        assertThat(DependencyRules.businessDomainOf(ROOT + "inventory.domain.model")).isEqualTo("inventory");
        assertThat(DependencyRules.businessDomainOf(ROOT + "product.product.application.port.in")).isEqualTo("product");
    }

    @ParameterizedTest
    @CsvSource({
            "inspection.adapter.out.external, lot.application.port.in",
            "inspection.adapter.in.event, inbound.domain.event",
            "lot.adapter.out.external, product.product.application.port.in",
            "inventory.adapter.in.web, inventory.domain.model",
            "outbound.adapter.out.persistence, global.error"
    })
    void 허용된_경로의_의존은_통과한다(String origin, String target) {
        assertThat(DependencyRules.isAllowedCrossDomainDependency(ROOT + origin, ROOT + target)).isTrue();
    }

    @ParameterizedTest
    @CsvSource({
            "inspection.adapter.out.external, lot.domain.model",
            "inspection.adapter.out.external, lot.application.port.out",
            "storage.adapter.out.external, product.product.domain.enums",
            "inspection.adapter.in.event, inbound.application.port.in",
            "order.adapter.in.web, inventory.application.port.in",
            "outbound.application.service, inventory.application.port.in",
            "outbound.domain.model, inventory.domain.model"
    })
    void 허용되지_않은_경로의_도메인_간_의존은_거부한다(String origin, String target) {
        assertThat(DependencyRules.isAllowedCrossDomainDependency(ROOT + origin, ROOT + target)).isFalse();
    }
}
