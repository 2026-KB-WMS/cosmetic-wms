package com.kb.cosmetic_wms.outbound;

import com.kb.cosmetic_wms.outbound.domain.exception.OutboundInsufficientStockException;
import com.kb.cosmetic_wms.outbound.domain.model.AvailableStock;
import com.kb.cosmetic_wms.outbound.domain.model.OutboundLine;
import com.kb.cosmetic_wms.outbound.domain.service.FefoAllocationSelector;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FefoAllocationSelectorTest {

    private static final Long ORDER_ITEM_ID = 11L;

    private final FefoAllocationSelector selector = new FefoAllocationSelector();

    @Test
    void 임박_재고를_먼저_소진하고_부족분은_다음_재고에서_분할_할당한다() {
        List<AvailableStock> stocks = List.of(
                new AvailableStock(1L, 20),
                new AvailableStock(2L, 50)
        );

        List<OutboundLine> lines = selector.select(ORDER_ITEM_ID, 30, stocks);

        assertThat(lines).containsExactly(
                new OutboundLine(ORDER_ITEM_ID, 1L, 20),
                new OutboundLine(ORDER_ITEM_ID, 2L, 10)
        );
    }

    @Test
    void 첫_재고로_요구_수량을_충족하면_한_건만_할당한다() {
        List<AvailableStock> stocks = List.of(
                new AvailableStock(1L, 50),
                new AvailableStock(2L, 50)
        );

        List<OutboundLine> lines = selector.select(ORDER_ITEM_ID, 30, stocks);

        assertThat(lines).containsExactly(new OutboundLine(ORDER_ITEM_ID, 1L, 30));
    }

    @Test
    void 전체_가용_재고를_정확히_소진하는_요구_수량도_할당에_성공한다() {
        List<AvailableStock> stocks = List.of(
                new AvailableStock(1L, 10),
                new AvailableStock(2L, 20)
        );

        List<OutboundLine> lines = selector.select(ORDER_ITEM_ID, 30, stocks);

        assertThat(lines).containsExactly(
                new OutboundLine(ORDER_ITEM_ID, 1L, 10),
                new OutboundLine(ORDER_ITEM_ID, 2L, 20)
        );
    }

    @Test
    void 가용_재고_합이_요구_수량보다_적으면_예외를_던진다() {
        List<AvailableStock> stocks = List.of(new AvailableStock(1L, 10));

        assertThatThrownBy(() -> selector.select(ORDER_ITEM_ID, 30, stocks))
                .isInstanceOf(OutboundInsufficientStockException.class);
    }

    @Test
    void 가용_재고가_없으면_예외를_던진다() {
        assertThatThrownBy(() -> selector.select(ORDER_ITEM_ID, 1, List.of()))
                .isInstanceOf(OutboundInsufficientStockException.class);
    }
}
