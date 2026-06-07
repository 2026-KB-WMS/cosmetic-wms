package com.kb.cosmetic_wms.domain.outbound;

import com.kb.cosmetic_wms.domain.outbound.constants.OutboundConstants;
import com.kb.cosmetic_wms.domain.outbound.entity.Outbound;
import com.kb.cosmetic_wms.domain.outbound.enums.OutboundStatus;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OutboundEntityTest {

    @Test
    void 출고를_정상적인_값으로_생성하면_최초_상태는_PENDING이고_출고일시는_null이어야_한다() {
        // given
        Long orderId = 1L;
        Long warehouseId = 10L;

        // when
        Outbound outbound = Outbound.create(orderId, warehouseId);

        // then
        assertThat(outbound.getOrderId()).isEqualTo(orderId);
        assertThat(outbound.getOutboundStatus()).isEqualTo(OutboundStatus.PENDING);
        assertThat(outbound.getOutboundDate()).isNull();
    }

    @Test
    void 출고_객체_생성_시_발주_ID가_누락되면_예외를_던진다() {
        // given
        Long warehouseId = 10L;

        // when & then
        Assertions.assertThatThrownBy(() ->
                        Outbound.create(null, warehouseId)
                )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(OutboundConstants.ORDER_ID_REQUIRED_MESSAGE);
    }

    @Test
    void 출고_객체_생성_시_창고_ID가_누락되면_예외를_던진다() {
        // given
        Long orderId = 1L;

        // when & then
        Assertions.assertThatThrownBy(() ->
                        Outbound.create(orderId, null)
                )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(OutboundConstants.WAREHOUSE_REQUIRED_ID_MESSAGE);
    }

    @Test
    void 출고_피킹_작업을_시작하면_상태가_PENDING에서_PICKING으로_변경되어야_한다() {
        // given
        Outbound outbound = Outbound.create(1L, 10L);

        // when
        outbound.startPicking();

        // then
        assertThat(outbound.getOutboundStatus()).isEqualTo(OutboundStatus.PICKING);
    }

    @Test
    void 이미_출고_완료된_전표는_다시_피킹을_시작할_수_없고_예외를_던진다() {
        // given
        Outbound outbound = Outbound.create(1L, 10L);
        outbound.startPicking();
        outbound.ship();

        // when & then
        Assertions.assertThatThrownBy(outbound::startPicking
                )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(OutboundConstants.INVALID_START_PICKING_MESSAGE);
    }

    @Test
    void 출고_완료_시_상태가_SHIPPED로_변하고_출고일시가_현재시간으로_기록된다() {
        // given
        Outbound outbound = Outbound.create(1L, 10L);
        outbound.startPicking();

        // when
        outbound.ship();

        // then
        assertThat(outbound.getOutboundStatus()).isEqualTo(OutboundStatus.SHIPPED);
        assertThat(outbound.getOutboundDate()).isNotNull();
    }

    @Test
    void 출고_대기_상태에서는_출고를_취소할_수_있다() {
        // given
        Outbound outbound = Outbound.create(1L, 10L);

        // when
        outbound.cancel();

        // then
        assertThat(outbound.getOutboundStatus()).isEqualTo(OutboundStatus.CANCELED);
    }

    @Test
    void 이미_피킹_중이거나_출고_완료된_전표는_취소할_수_없고_예외를_던진다() {
        // given
        Outbound outbound = Outbound.create(1L, 10L);
        outbound.startPicking();

        // when & then
        Assertions.assertThatThrownBy(outbound::cancel
                )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(OutboundConstants.INVALID_CANCEL_MESSAGE);
    }

    @Test
    void 취소된_상태에서는_어떠한_상태로도_전이할_수_없고_예외를_던진다() {
        // given
        Outbound outbound = Outbound.create(1L, 10L);
        outbound.cancel();

        // when & then (CANCELED -> PICKING 시도 시 차단 검증)
        Assertions.assertThatThrownBy(outbound::startPicking
                )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(OutboundConstants.INVALID_START_PICKING_MESSAGE);

        // when & then (CANCELED -> SHIPPED 시도 시 차단 검증)
        Assertions.assertThatThrownBy(outbound::ship
                )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(OutboundConstants.INVALID_SHIP_MESSAGE);
    }
}
