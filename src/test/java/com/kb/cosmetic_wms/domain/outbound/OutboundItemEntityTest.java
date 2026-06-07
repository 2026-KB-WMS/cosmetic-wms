package com.kb.cosmetic_wms.domain.outbound;

import com.kb.cosmetic_wms.domain.outbound.constants.OutboundConstants;
import com.kb.cosmetic_wms.domain.outbound.entity.Outbound;
import com.kb.cosmetic_wms.domain.outbound.entity.OutboundItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class OutboundItemEntityTest {

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100})
    void 출고_상세_항목_생성_시_지시_수량이_0_이하이면_예외를_던진다(int invalidQuantity) {
        // given
        Outbound outbound = Outbound.create(1L, 10L);
        Long orderItemId = 1L;
        Long inventoryId = 1L;

        // when & then
        assertThatThrownBy(() ->
                OutboundItem.create(outbound, orderItemId, inventoryId, invalidQuantity)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(OutboundConstants.INVALID_TARGET_QUANTITY_MESSAGE);
    }

    @Test
    void 실제_피킹_수량이_지시_수량을_초과하여_변경하려고_하면_예외를_던진다() {
        // given
        int targetQuantity = 10;
        Outbound outbound = Outbound.create(1L, 10L);
        OutboundItem outboundItem = OutboundItem.create(
                outbound, 1L, 1L, targetQuantity);

        // when & then
        assertThatThrownBy(() ->
                outboundItem.changePickedQuantity(11)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(OutboundConstants.EXCEED_PICKED_QUANTITY_MESSAGE);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -5, -100})
    void 실제_피킹_수량을_음수로_변경하려고_하면_예외를_던진다(int invalidPickedQuantity) {
        // given
        int targetQuantity = 10;
        Outbound outbound = Outbound.create(1L, 10L);
        OutboundItem outboundItem = OutboundItem.create(
                outbound, 1L, 1L, targetQuantity);

        // when & then
        assertThatThrownBy(() ->
                outboundItem.changePickedQuantity(invalidPickedQuantity)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(OutboundConstants.INVALID_PICKED_QUANTITY_MESSAGE);
    }

    @Test
    void 출고_전표가_피킹_중_상태가_아닐_때_실제_피킹_수량을_변경하려고_하면_예외를_던진다() {
        // given
        Outbound outbound = Outbound.create(1L, 10L);
        OutboundItem outboundItem = OutboundItem.create(
                outbound, 1L, 1L, 10);

        // when & then
        assertThatThrownBy(() ->
                outboundItem.changePickedQuantity(5)
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(OutboundConstants.INVALID_PICKING_STATUS_MESSAGE);
    }

    @Test
    void 지시_수량과_실제_피킹_수량이_일치하면_완전_피킹_여부가_true를_반환한다() {
        // given
        Outbound outbound = Outbound.create(1L, 10L);
        outbound.startPicking();

        OutboundItem outboundItem = OutboundItem.create(
                outbound, 1L, 1L, 10);

        // when
        outboundItem.changePickedQuantity(10);

        // then
        assertThat(outboundItem.isFullyPicked()).isTrue();
    }
}
