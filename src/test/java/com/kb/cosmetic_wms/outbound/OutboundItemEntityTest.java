package com.kb.cosmetic_wms.outbound;

import com.kb.cosmetic_wms.outbound.domain.enums.OutboundType;
import com.kb.cosmetic_wms.outbound.domain.exception.OutboundExceedPickedQuantityException;
import com.kb.cosmetic_wms.outbound.domain.exception.OutboundInvalidPickedQuantityException;
import com.kb.cosmetic_wms.outbound.domain.exception.OutboundItemNotFoundException;
import com.kb.cosmetic_wms.outbound.domain.exception.OutboundInvalidTargetQuantityException;
import com.kb.cosmetic_wms.outbound.domain.exception.OutboundPickingNotAllowedException;
import com.kb.cosmetic_wms.outbound.domain.model.Outbound;
import com.kb.cosmetic_wms.outbound.domain.model.OutboundLine;
import com.kb.cosmetic_wms.outbound.fixture.OutboundTestBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class OutboundItemEntityTest {

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100})
    void 출고_상세_항목_생성_시_지시_수량이_0_이하이면_예외를_던진다(int invalidQuantity) {
        OutboundLine line = new OutboundLine(1L, 1L, invalidQuantity);

        assertThatThrownBy(() -> Outbound.create(1L, 10L, OutboundType.ORDER, List.of(line)))
                .isInstanceOf(OutboundInvalidTargetQuantityException.class);
    }

    @Test
    void 실제_피킹_수량이_지시_수량을_초과하여_변경하려고_하면_예외를_던진다() {
        Long itemId = 1L;
        Outbound outbound = OutboundTestBuilder.withItemIds(Outbound.create(1L, 10L, OutboundType.ORDER,
                List.of(new OutboundLine(1L, 1L, 10))));
        outbound.allocate();
        outbound.startProcessing();

        assertThatThrownBy(() -> outbound.changeItemPickedQuantity(itemId, 11))
                .isInstanceOf(OutboundExceedPickedQuantityException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -5, -100})
    void 실제_피킹_수량을_음수로_변경하려고_하면_예외를_던진다(int invalidPickedQuantity) {
        Long itemId = 1L;
        Outbound outbound = OutboundTestBuilder.withItemIds(Outbound.create(1L, 10L, OutboundType.ORDER,
                List.of(new OutboundLine(1L, 1L, 10))));
        outbound.allocate();
        outbound.startProcessing();

        assertThatThrownBy(() -> outbound.changeItemPickedQuantity(itemId, invalidPickedQuantity))
                .isInstanceOf(OutboundInvalidPickedQuantityException.class);
    }

    @Test
    void 출고_전표가_출고_준비_중_상태가_아닐_때_실제_피킹_수량을_변경하려고_하면_예외를_던진다() {
        Long itemId = 1L;
        Outbound outbound = OutboundTestBuilder.withItemIds(Outbound.create(1L, 10L, OutboundType.ORDER,
                List.of(new OutboundLine(1L, 1L, 10))));
        // PENDING 상태: startProcessing 미호출

        assertThatThrownBy(() -> outbound.changeItemPickedQuantity(itemId, 5))
                .isInstanceOf(OutboundPickingNotAllowedException.class);
    }

    @Test
    void 지시_수량과_실제_피킹_수량이_일치하면_완전_피킹_여부가_true를_반환한다() {
        Long itemId = 1L;
        Outbound outbound = OutboundTestBuilder.withItemIds(Outbound.create(1L, 10L, OutboundType.ORDER,
                List.of(new OutboundLine(1L, 1L, 10))));
        outbound.allocate();
        outbound.startProcessing();

        outbound.changeItemPickedQuantity(itemId, 10);

        assertThat(outbound.getOutboundItems().getFirst().isFullyPicked()).isTrue();
    }

    @Test
    void 같은_재고를_가리키는_품목이_둘이어도_품목_ID로_지정한_품목만_피킹된다() {
        Outbound outbound = OutboundTestBuilder.withItemIds(Outbound.create(1L, 10L, OutboundType.ORDER,
                List.of(new OutboundLine(1L, 7L, 10), new OutboundLine(2L, 7L, 4))));
        outbound.allocate();
        outbound.startProcessing();

        outbound.changeItemPickedQuantity(2L, 4);

        assertThat(outbound.getOutboundItems().get(0).getPickedQuantity()).isZero();
        assertThat(outbound.getOutboundItems().get(1).getPickedQuantity()).isEqualTo(4);
    }

    @Test
    void 존재하지_않는_품목_ID로_피킹_수량을_변경하면_예외를_던진다() {
        Outbound outbound = new OutboundTestBuilder().buildProcessing();

        assertThatThrownBy(() -> outbound.changeItemPickedQuantity(999L, 5))
                .isInstanceOf(OutboundItemNotFoundException.class);
    }

    @Test
    void 피킹_수량을_같은_값으로_다시_설정해도_결과가_같다() {
        Outbound outbound = new OutboundTestBuilder().buildProcessing();

        outbound.changeItemPickedQuantity(1L, 6);
        outbound.changeItemPickedQuantity(1L, 6);

        assertThat(outbound.getOutboundItems().getFirst().getPickedQuantity()).isEqualTo(6);
    }
}
