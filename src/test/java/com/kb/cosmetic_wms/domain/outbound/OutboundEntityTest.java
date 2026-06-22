package com.kb.cosmetic_wms.domain.outbound;

import com.kb.cosmetic_wms.domain.outbound.constants.OutboundConstants;
import com.kb.cosmetic_wms.domain.outbound.entity.Outbound;
import com.kb.cosmetic_wms.domain.outbound.entity.OutboundItem;
import com.kb.cosmetic_wms.domain.outbound.enums.OutboundStatus;
import com.kb.cosmetic_wms.domain.outbound.enums.OutboundType;
import com.kb.cosmetic_wms.domain.outbound.exception.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class OutboundEntityTest {

    @Test
    void 출고를_정상적인_값으로_생성하면_최초_상태는_PENDING이고_출고일시는_null이어야_한다() {
        Outbound outbound = Outbound.create(1L, 10L, OutboundType.ORDER);

        assertThat(outbound.getOrdersId()).isEqualTo(1L);
        assertThat(outbound.getOutboundType()).isEqualTo(OutboundType.ORDER);
        assertThat(outbound.getOutboundStatus()).isEqualTo(OutboundStatus.PENDING);
        assertThat(outbound.getOutboundDate()).isNull();
    }

    @Test
    void 출고_객체_생성_시_발주_ID가_누락되면_예외를_던진다() {
        assertThatThrownBy(() -> Outbound.create(null, 10L, OutboundType.ORDER))
                .isInstanceOf(OutboundOrderIdRequiredException.class);
    }

    @Test
    void 출고_객체_생성_시_창고_ID가_누락되면_예외를_던진다() {
        assertThatThrownBy(() -> Outbound.create(1L, null, OutboundType.ORDER))
                .isInstanceOf(OutboundWarehouseRequiredException.class);
    }

    @Test
    void 출고_객체_생성_시_출고_유형이_누락되면_예외를_던진다() {
        assertThatThrownBy(() -> Outbound.create(1L, 10L, null))
                .isInstanceOf(OutboundTypeRequiredException.class);
    }

    @Test
    void 재고_할당_처리를_하면_상태가_PENDING에서_ALLOCATED로_변경되어야_한다() {
        Outbound outbound = Outbound.create(1L, 10L, OutboundType.ORDER);

        outbound.allocate();

        assertThat(outbound.getOutboundStatus()).isEqualTo(OutboundStatus.ALLOCATED);
    }

    @Test
    void 출고_작업을_시작하면_상태가_ALLOCATED에서_PROCESSING으로_변경되어야_한다() {
        Outbound outbound = Outbound.create(1L, 10L, OutboundType.ORDER);
        outbound.allocate();

        outbound.startProcessing();

        assertThat(outbound.getOutboundStatus()).isEqualTo(OutboundStatus.PROCESSING);
    }

    @Test
    void 이미_출하_완료된_전표는_재고_할당을_다시_시도할_수_없고_예외를_던진다() {
        Outbound outbound = Outbound.create(1L, 10L, OutboundType.ORDER);
        outbound.allocate();
        outbound.startProcessing();
        outbound.ship();

        assertThatThrownBy(outbound::allocate)
                .isInstanceOf(OutboundAllocateNotAllowedException.class);
    }

    @Test
    void 출고_완료_시_상태가_SHIPPED로_변하고_출고일시가_현재시간으로_기록된다() {
        Outbound outbound = Outbound.create(1L, 10L, OutboundType.ORDER);
        outbound.allocate();
        outbound.startProcessing();

        outbound.ship();

        assertThat(outbound.getOutboundStatus()).isEqualTo(OutboundStatus.SHIPPED);
        assertThat(outbound.getOutboundDate()).isNotNull();
    }

    @Test
    void 출고_대기_상태에서는_출고를_취소할_수_있다() {
        Outbound outbound = Outbound.create(1L, 10L, OutboundType.ORDER);

        outbound.cancel();

        assertThat(outbound.getOutboundStatus()).isEqualTo(OutboundStatus.CANCELED);
    }

    @Test
    void 재고_할당_상태에서도_출고를_취소할_수_있다() {
        Outbound outbound = Outbound.create(1L, 10L, OutboundType.ORDER);
        outbound.allocate();

        outbound.cancel();

        assertThat(outbound.getOutboundStatus()).isEqualTo(OutboundStatus.CANCELED);
    }

    @Test
    void 이미_출고_준비_중인_전표는_취소할_수_없고_예외를_던진다() {
        Outbound outbound = Outbound.create(1L, 10L, OutboundType.ORDER);
        outbound.allocate();
        outbound.startProcessing();

        assertThatThrownBy(outbound::cancel)
                .isInstanceOf(OutboundCancelNotAllowedException.class);
    }

    @Test
    void 취소된_상태에서는_어떠한_상태로도_전이할_수_없고_예외를_던진다() {
        Outbound outbound = Outbound.create(1L, 10L, OutboundType.ORDER);
        outbound.cancel();

        assertThatThrownBy(outbound::allocate)
                .isInstanceOf(OutboundAllocateNotAllowedException.class);

        assertThatThrownBy(outbound::ship)
                .isInstanceOf(OutboundShipNotAllowedException.class);
    }

    @Test
    void 출고_대기_상태에서는_출고_상세_항목을_정상적으로_추가할_수_있고_자식_리스트에_추가한다() {
        Outbound outbound = Outbound.create(1L, 10L, OutboundType.ORDER);
        OutboundLine line = new OutboundLine(1L, 1L, 5);

        OutboundItem addedItem = outbound.addItem(line);

        assertThat(outbound.getOutboundItems()).hasSize(1);
        assertThat(outbound.getOutboundItems().getFirst()).isEqualTo(addedItem);
        assertThat(addedItem.getTargetQuantity()).isEqualTo(5);
        assertThat(addedItem.getPickedQuantity()).isZero();
    }

    @Test
    void 출고_대기_상태가_아닐_때_품목을_추가하려고_하면_예외를_던진다() {
        Outbound outbound = Outbound.create(1L, 10L, OutboundType.ORDER);
        outbound.allocate();

        OutboundLine line = new OutboundLine(1L, 1L, 5);

        assertThatThrownBy(() -> outbound.addItem(line))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(OutboundConstants.INVALID_ADD_ITEM_STATUS_MESSAGE);
    }

    @Test
    void 모든_출고_품목의_피킹이_완료되지_않은_상태에서_출고를_시도하면_예외를_던진다() {
        Outbound outbound = Outbound.create(1L, 10L, OutboundType.ORDER);
        OutboundLine line = new OutboundLine(1L, 1L, 10);
        OutboundItem item = outbound.addItem(line);
        outbound.allocate();
        outbound.startProcessing();
        item.changePickedQuantity(8);

        assertThatThrownBy(outbound::ship)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(OutboundConstants.INCOMPLETE_PICKING_MESSAGE);
    }

    @Test
    void 모든_출고_품목의_피킹이_완료된_상태에서만_출고_처리가_성공한다() {
        Outbound outbound = Outbound.create(1L, 10L, OutboundType.ORDER);
        OutboundItem item1 = outbound.addItem(new OutboundLine(1L, 1L, 10));
        OutboundItem item2 = outbound.addItem(new OutboundLine(2L, 2L, 5));
        outbound.allocate();
        outbound.startProcessing();
        item1.changePickedQuantity(10);
        item2.changePickedQuantity(5);

        outbound.ship();

        assertThat(outbound.getOutboundStatus()).isEqualTo(OutboundStatus.SHIPPED);
        assertThat(outbound.getOutboundDate()).isNotNull();
    }
}
