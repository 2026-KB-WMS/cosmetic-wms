package com.kb.cosmetic_wms.outbound;

import com.kb.cosmetic_wms.outbound.domain.enums.OutboundType;
import com.kb.cosmetic_wms.outbound.domain.exception.OutboundExceedPickedQuantityException;
import com.kb.cosmetic_wms.outbound.domain.exception.OutboundInvalidPickedQuantityException;
import com.kb.cosmetic_wms.outbound.domain.exception.OutboundInvalidTargetQuantityException;
import com.kb.cosmetic_wms.outbound.domain.exception.OutboundPickingNotAllowedException;
import com.kb.cosmetic_wms.outbound.domain.model.Outbound;
import com.kb.cosmetic_wms.outbound.domain.model.OutboundLine;
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
        Long inventoryId = 1L;
        Outbound outbound = Outbound.create(1L, 10L, OutboundType.ORDER,
                List.of(new OutboundLine(1L, inventoryId, 10)));
        outbound.allocate();
        outbound.startProcessing();

        assertThatThrownBy(() -> outbound.changeItemPickedQuantity(inventoryId, 11))
                .isInstanceOf(OutboundExceedPickedQuantityException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -5, -100})
    void 실제_피킹_수량을_음수로_변경하려고_하면_예외를_던진다(int invalidPickedQuantity) {
        Long inventoryId = 1L;
        Outbound outbound = Outbound.create(1L, 10L, OutboundType.ORDER,
                List.of(new OutboundLine(1L, inventoryId, 10)));
        outbound.allocate();
        outbound.startProcessing();

        assertThatThrownBy(() -> outbound.changeItemPickedQuantity(inventoryId, invalidPickedQuantity))
                .isInstanceOf(OutboundInvalidPickedQuantityException.class);
    }

    @Test
    void 출고_전표가_출고_준비_중_상태가_아닐_때_실제_피킹_수량을_변경하려고_하면_예외를_던진다() {
        Long inventoryId = 1L;
        Outbound outbound = Outbound.create(1L, 10L, OutboundType.ORDER,
                List.of(new OutboundLine(1L, inventoryId, 10)));
        // PENDING 상태: startProcessing 미호출

        assertThatThrownBy(() -> outbound.changeItemPickedQuantity(inventoryId, 5))
                .isInstanceOf(OutboundPickingNotAllowedException.class);
    }

    @Test
    void 지시_수량과_실제_피킹_수량이_일치하면_완전_피킹_여부가_true를_반환한다() {
        Long inventoryId = 1L;
        Outbound outbound = Outbound.create(1L, 10L, OutboundType.ORDER,
                List.of(new OutboundLine(1L, inventoryId, 10)));
        outbound.allocate();
        outbound.startProcessing();

        outbound.changeItemPickedQuantity(inventoryId, 10);

        assertThat(outbound.getOutboundItems().getFirst().isFullyPicked()).isTrue();
    }
}
