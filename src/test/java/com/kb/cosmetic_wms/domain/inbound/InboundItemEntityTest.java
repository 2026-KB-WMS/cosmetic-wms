package com.kb.cosmetic_wms.domain.inbound;

import com.kb.cosmetic_wms.domain.inbound.constants.InboundConstants;
import com.kb.cosmetic_wms.domain.inbound.entity.Inbound;
import com.kb.cosmetic_wms.domain.inbound.entity.InboundItem;
import com.kb.cosmetic_wms.domain.inbound.enums.InspectionStatus;
import com.kb.cosmetic_wms.domain.inventory.entity.Lot;
import com.kb.cosmetic_wms.domain.inventory.fixture.LotTestBuilder;
import com.kb.cosmetic_wms.domain.partner.entity.Partner;
import com.kb.cosmetic_wms.domain.partner.enums.PartnerType;
import com.kb.cosmetic_wms.domain.product.entity.Product;
import com.kb.cosmetic_wms.domain.product.fixture.ProductTestBuilder;
import com.kb.cosmetic_wms.domain.storage.entity.Section;
import com.kb.cosmetic_wms.domain.storage.entity.Warehouse;
import com.kb.cosmetic_wms.domain.storage.fixture.SectionTestBuilder;
import com.kb.cosmetic_wms.domain.storage.fixture.WarehouseTestBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

        assertThatThrownBy(() ->
                InboundItem.create(inbound, PRODUCT, invalidQuantity, MANUFACTURE_DATE, EXPIRATION_DATE)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(InboundConstants.INVALID_INBOUND_QUANTITY_MESSAGE);
    }

    @Test
    void 실물_적재_시_로트와_섹션_정보가_입력되면_검수_상태가_INSPECTING으로_변경된다() {
        // given
        Inbound inbound = createStubInbound();
        InboundItem inboundItem = InboundItem.create(inbound, PRODUCT, QUANTITY, MANUFACTURE_DATE, EXPIRATION_DATE);

        Lot lot = new LotTestBuilder().build();
        Section section = new SectionTestBuilder().build();

        // when
        inboundItem.completePutaway(lot, section);

        // then
        assertThat(inboundItem.getInspectionStatus()).isEqualTo(InspectionStatus.INSPECTING);
        assertThat(inboundItem.getLot()).isEqualTo(lot);
        assertThat(inboundItem.getSection()).isEqualTo(section);
    }

    @Test
    void 검수_대기_상태가_아닌_상품을_실물_적재_시도하면_예외를_던진다() {
        // given
        Inbound inbound = createStubInbound();
        InboundItem inboundItem = InboundItem.create(inbound, PRODUCT, QUANTITY, MANUFACTURE_DATE, EXPIRATION_DATE);

        Lot lot = new LotTestBuilder().build();
        Section section = new SectionTestBuilder().build();

        inboundItem.completePutaway(lot, section);

        // when & then
        assertThatThrownBy(() -> inboundItem.completePutaway(lot, section))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 적재가 완료되었거나 검수가 진행된 품목입니다.");
    }

    @Test
    void 검수_중_상태에서는_품질_판정_결과에_따라_NORMAL_상태로_완료된다() {
        // given
        Inbound inbound = createStubInbound();
        InboundItem inboundItem = InboundItem.create(inbound, PRODUCT, QUANTITY, MANUFACTURE_DATE, EXPIRATION_DATE);
        inboundItem.completePutaway(new LotTestBuilder().build(), new SectionTestBuilder().build());

        // when
        inboundItem.changeToNormal();

        // then
        assertThat(inboundItem.getInspectionStatus()).isEqualTo(InspectionStatus.NORMAL);
    }

    @Test
    void 입고_상세_생성_시_입고_객체가_누락되면_예외를_던진다() {
        assertThatThrownBy(() ->
                InboundItem.create(null, PRODUCT, QUANTITY, MANUFACTURE_DATE, EXPIRATION_DATE)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(InboundConstants.INBOUND_MASTER_REQUIRED_MESSAGE);
    }

    @Test
    void 입고_상세_생성_시_상품_정보가_누락되면_예외를_던진다() {
        // given
        Inbound inbound = createStubInbound();

        assertThatThrownBy(() ->
                InboundItem.create(inbound, null, QUANTITY, MANUFACTURE_DATE, EXPIRATION_DATE)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(InboundConstants.INBOUND_PRODUCT_REQUIRED_MESSAGE);
    }

    @Test
    void 실물_적재_시_로트_정보가_누락되면_예외를_던진다() {
        // given
        Inbound inbound = createStubInbound();
        InboundItem inboundItem = InboundItem.create(inbound, PRODUCT, QUANTITY, MANUFACTURE_DATE, EXPIRATION_DATE);

        // when & then
        assertThatThrownBy(() ->
                inboundItem.completePutaway(null, new SectionTestBuilder().build())
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(InboundConstants.PUTAWAY_LOT_REQUIRED_MESSAGE);
    }

    @Test
    void 실물_적재_시_섹션_정보가_누락되면_예외를_던진다() {
        // given
        Inbound inbound = createStubInbound();
        InboundItem inboundItem = InboundItem.create(inbound, PRODUCT, QUANTITY, MANUFACTURE_DATE, EXPIRATION_DATE);

        // when & then
        assertThatThrownBy(() ->
                inboundItem.completePutaway(new LotTestBuilder().build(), null)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(InboundConstants.PUTAWAY_SECTION_REQUIRED_MESSAGE);
    }

    @Test
    void 검수_중_상태에서는_품질_판정_결과에_따라_HOLD_상태로_완료된다() {
        // given
        Inbound inbound = createStubInbound();
        InboundItem inboundItem = InboundItem.create(inbound, PRODUCT, QUANTITY, MANUFACTURE_DATE, EXPIRATION_DATE);
        inboundItem.completePutaway(new LotTestBuilder().build(), new SectionTestBuilder().build());

        // when
        inboundItem.changeToHold();

        // then
        assertThat(inboundItem.getInspectionStatus()).isEqualTo(InspectionStatus.HOLD);
    }

    @Test
    void 검수_중_상태가_아닌_대기_또는_완료_상태의_상품을_품질_판정_시도하면_예외를_던진다() {
        // given
        Inbound inbound = createStubInbound();
        InboundItem inboundItem = InboundItem.create(inbound, PRODUCT, QUANTITY, MANUFACTURE_DATE, EXPIRATION_DATE);

        // when & then
        assertThatThrownBy(inboundItem::changeToHold)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(InboundConstants.INVALID_HOLD_STATUS_MESSAGE);
    }
}
