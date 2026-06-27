package com.kb.cosmetic_wms.inbound;

import com.kb.cosmetic_wms.inbound.domain.enums.InspectionStatus;
import com.kb.cosmetic_wms.inbound.domain.exception.*;
import com.kb.cosmetic_wms.inbound.domain.model.Inbound;
import com.kb.cosmetic_wms.inbound.domain.model.InboundItem;
import com.kb.cosmetic_wms.inbound.domain.model.InboundLine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InboundItemEntityTest {

    private static final Long PRODUCT_ID = 1L;
    private static final Long LOT_ID = 10L;
    private static final Long SECTION_ID = 10L;

    private static final int QUANTITY = 100;
    private static final LocalDate MANUFACTURE_DATE = LocalDate.now().minusDays(1);
    private static final LocalDate EXPIRATION_DATE = LocalDate.now().plusYears(3);
    private static final LocalDateTime INBOUND_DATE = LocalDateTime.now().plusYears(1);

    private Inbound createStubInbound() {
        return Inbound.create(INBOUND_DATE, 1L, 1L);
    }

    private InboundLine createStandardLine() {
        return new InboundLine(PRODUCT_ID, QUANTITY, MANUFACTURE_DATE, EXPIRATION_DATE);
    }

    @Test
    void 입고_상세_엔티티는_정상적인_값으로_생성_시_최초_검수_상태가_WAITING_이어야_한다() {
        // given
        Inbound inbound = createStubInbound();
        InboundLine line = createStandardLine();

        // when
        InboundItem inboundItem = inbound.addItem(line);

        // then
        assertThat(inboundItem.getInspectionStatus()).isEqualTo(InspectionStatus.WAITING);
        assertThat(inboundItem.getProductId()).isEqualTo(PRODUCT_ID);
        assertThat(inboundItem.getLotId()).isNull();
        assertThat(inboundItem.getSectionId()).isNull();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -5, -100})
    void 입고_상세_생성_시_예정_수량이_0_이하이면_예외를_던진다(int invalidQuantity) {
        // given
        Inbound inbound = createStubInbound();
        InboundLine line = new InboundLine(PRODUCT_ID, invalidQuantity, MANUFACTURE_DATE, EXPIRATION_DATE);

        assertThatThrownBy(() -> inbound.addItem(line))
                .isInstanceOf(InboundInvalidQuantityException.class)
                .hasMessage(InboundErrorCode.INBOUND_ITEM_INVALID_QUANTITY.getMessage());
    }

    @Test
    void 실물_적재_시_로트와_섹션_정보가_입력되면_검수_상태가_INSPECTING으로_변경된다() {
        // given
        Inbound inbound = createStubInbound();
        InboundItem inboundItem = inbound.addItem(createStandardLine());

        // when
        inboundItem.completePutaway(LOT_ID, SECTION_ID);

        // then
        assertThat(inboundItem.getInspectionStatus()).isEqualTo(InspectionStatus.INSPECTING);
        assertThat(inboundItem.getLotId()).isEqualTo(LOT_ID);
        assertThat(inboundItem.getSectionId()).isEqualTo(SECTION_ID);
    }

    @Test
    void 검수_대기_상태가_아닌_상품을_실물_적재_시도하면_예외를_던진다() {
        // given
        Inbound inbound = createStubInbound();
        InboundItem inboundItem = inbound.addItem(createStandardLine());

        inboundItem.completePutaway(LOT_ID, SECTION_ID);

        // when & then
        assertThatThrownBy(() -> inboundItem.completePutaway(LOT_ID, SECTION_ID))
                .isInstanceOf(InboundInvalidPutawayStatusException.class)
                .hasMessageContaining("이미 적재가 완료되었거나 검수가 진행된 품목입니다.");
    }

    @Test
    void 검수_중_상태에서는_품질_판정_결과에_따라_NORMAL_상태로_완료된다() {
        // given
        Inbound inbound = createStubInbound();
        InboundItem inboundItem = inbound.addItem(createStandardLine());
        inboundItem.completePutaway(LOT_ID, SECTION_ID);

        // when
        inboundItem.changeToNormal();

        // then
        assertThat(inboundItem.getInspectionStatus()).isEqualTo(InspectionStatus.NORMAL);
    }

    @Test
    void 입고_전표가_입고_예정_상태가_아닐_때_품목을_추가하려고_하면_예외를_던진다() {
        // given
        Inbound inbound = createStubInbound();
        inbound.startExecution();

        InboundLine line = createStandardLine();

        assertThatThrownBy(() -> inbound.addItem(line))
                .isInstanceOf(InboundInvalidAddItemStatusException.class)
                .hasMessage(InboundErrorCode.INBOUND_INVALID_ADD_ITEM_STATUS.getMessage());
    }

    @Test
    void 입고_상세_생성_시_상품_정보가_누락되면_예외를_던진다() {
        // given
        Inbound inbound = createStubInbound();
        InboundLine line = new InboundLine(null, QUANTITY, MANUFACTURE_DATE, EXPIRATION_DATE);

        // when & then
        assertThatThrownBy(() -> inbound.addItem(line))
                .isInstanceOf(InboundItemProductRequiredException.class)
                .hasMessage(InboundErrorCode.INBOUND_ITEM_PRODUCT_REQUIRED.getMessage());
    }

    @Test
    void 실물_적재_시_로트_정보가_누락되면_예외를_던진다() {
        // given
        Inbound inbound = createStubInbound();
        InboundItem inboundItem = inbound.addItem(createStandardLine());

        // when & then
        assertThatThrownBy(() -> inboundItem.completePutaway(null, SECTION_ID))
                .isInstanceOf(InboundPutawayLotRequiredException.class)
                .hasMessage(InboundErrorCode.INBOUND_PUTAWAY_LOT_REQUIRED.getMessage());
    }

    @Test
    void 실물_적재_시_섹션_정보가_누락되면_예외를_던진다() {
        // given
        Inbound inbound = createStubInbound();
        InboundItem inboundItem = inbound.addItem(createStandardLine());

        // when & then
        assertThatThrownBy(() -> inboundItem.completePutaway(LOT_ID, null))
                .isInstanceOf(InboundPutawaySectionRequiredException.class)
                .hasMessage(InboundErrorCode.INBOUND_PUTAWAY_SECTION_REQUIRED.getMessage());
    }

    @Test
    void 검수_중_상태에서는_품질_판정_결과에_따라_HOLD_상태로_완료된다() {
        // given
        Inbound inbound = createStubInbound();
        InboundItem inboundItem = inbound.addItem(createStandardLine());
        inboundItem.completePutaway(LOT_ID, SECTION_ID);

        // when
        inboundItem.changeToHold();

        // then
        assertThat(inboundItem.getInspectionStatus()).isEqualTo(InspectionStatus.HOLD);
    }

    @Test
    void 검수_중_상태가_아닌_대기_또는_완료_상태의_상품을_품질_판정_시도하면_예외를_던진다() {
        // given
        Inbound inbound = createStubInbound();
        InboundItem inboundItem = inbound.addItem(createStandardLine());

        // when & then
        assertThatThrownBy(inboundItem::changeToHold)
                .isInstanceOf(InboundInvalidHoldStatusException.class)
                .hasMessage(InboundErrorCode.INBOUND_INVALID_HOLD_STATUS.getMessage());
    }
}