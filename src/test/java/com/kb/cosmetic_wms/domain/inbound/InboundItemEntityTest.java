package com.kb.cosmetic_wms.domain.inbound;

import com.kb.cosmetic_wms.domain.inbound.constants.InboundConstants;
import com.kb.cosmetic_wms.domain.inbound.entity.Inbound;
import com.kb.cosmetic_wms.domain.inbound.entity.InboundItem;
import com.kb.cosmetic_wms.domain.inbound.enums.InspectionStatus;
import com.kb.cosmetic_wms.domain.partner.entity.Partner;
import com.kb.cosmetic_wms.domain.partner.enums.PartnerType;
import com.kb.cosmetic_wms.domain.product.entity.Product;
import com.kb.cosmetic_wms.domain.product.fixture.ProductTestBuilder;
import com.kb.cosmetic_wms.domain.storage.entity.Warehouse;
import com.kb.cosmetic_wms.domain.storage.fixture.WarehouseTestBuilder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class InboundItemEntityTest {

    private static final Product PRODUCT = new ProductTestBuilder().build();
    private static final int QUANTITY = 100;
    private static final LocalDateTime MANUFACTURE_DATE =
            LocalDateTime.of(2026, 6, 1, 0, 0);
    private static final LocalDateTime EXPIRATION_DATE =
            LocalDateTime.of(2027, 6, 1, 0, 0);

    private Inbound createStubInbound() {
        Warehouse warehouse = new WarehouseTestBuilder().build();
        Partner partner = Partner.create("본사", PartnerType.HEADQUARTER, "123-45-67890");
        return Inbound.create(LocalDateTime.of(2030, 6, 1, 0, 0), warehouse, partner);
    }

    @Test
    void 입고_상세_엔티티는_정상적인_값으로_생성_시_최초_검수_상태가_WAITING_이어야_한다() {
        // given
        Inbound inbound = createStubInbound();

        // when
        InboundItem inboundItem = InboundItem.create(inbound, PRODUCT, QUANTITY, MANUFACTURE_DATE, EXPIRATION_DATE);

        // then
        assertThat(inboundItem.getInspectionStatus()).isEqualTo(InspectionStatus.WAITING);
        assertThat(inboundItem.getLot()).isNull();
        assertThat(inboundItem.getSection()).isNull();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -5, -100})
    void 입고_상세_생성_시_예정_수량이_0_이하이면_예외를_던진다(int invalidQuantity) {
        // given
        Inbound inbound = createStubInbound();

        Assertions.assertThatThrownBy(() ->
                        InboundItem.create(inbound, PRODUCT, invalidQuantity, MANUFACTURE_DATE, EXPIRATION_DATE)
                )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(InboundConstants.INVALID_INBOUND_QUANTITY_MESSAGE);
    }
}
