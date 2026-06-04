package com.kb.cosmetic_wms.domain.order;

import com.kb.cosmetic_wms.domain.order.constants.OrderConstants;
import com.kb.cosmetic_wms.domain.order.entity.Order;
import com.kb.cosmetic_wms.domain.order.enums.OrderStatus;
import com.kb.cosmetic_wms.domain.order.fixture.OrderTestBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class OrderEntityTest {

    @Test
    void 발주를_정상적인_값으로_생성하면_최초_상태는_PENDING_이어야_한다() {
        // given & when
        Order order = new OrderTestBuilder().build();

        // then
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getOrderItems().getFirst().getOrder()).isEqualTo(order);
    }

    @Test
    void 발주_생성_시_가맹점_정보가_누락되면_예외를_던진다() {
        assertThatThrownBy(() ->
                new OrderTestBuilder()
                        .store(null)
                        .build()
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(OrderConstants.STORE_REQUIRED_MESSAGE);
    }

    @Test
    void 발주_생성_시_배정_창고_정보가_누락되면_예외를_던진다() {
        assertThatThrownBy(() ->
                new OrderTestBuilder()
                        .warehouse(null)
                        .build()
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(OrderConstants.WAREHOUSE_REQUIRED_MESSAGE);
    }

    @Test
    void 발주_생성_시_발주_항목_리스트가_null이거나_비어있으면_예외를_던진다() {
        assertThatThrownBy(() ->
                new OrderTestBuilder()
                        .emptyOrderLines()
                        .build()
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(OrderConstants.ORDER_ITEM_MINIMUM_MESSAGE);
    }

    @Test
    void 발주_대기_상태에서는_발주를_취소할_수_있다() {
        // given
        Order order = new OrderTestBuilder().build();

        // when
        order.cancel();

        // then
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELED);
    }

    @Test
    void 이미_작업이_시작된_발주_건은_취소_시_예외를_던진다() {
        // given
        Order order = new OrderTestBuilder().build();
        order.startOrderProcess();

        // when & then
        assertThatThrownBy(order::cancel)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(OrderConstants.INVALID_CANCEL_STATUS_MESSAGE);
    }

    @Test
    void 배송_중인_발주_건은_취소_시_예외를_던진다() {
        // given
        Order order = new OrderTestBuilder().build();
        order.startOrderProcess();
        order.ship();

        // when & then
        assertThatThrownBy(order::cancel)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(OrderConstants.INVALID_CANCEL_STATUS_MESSAGE);
    }

    @Test
    void 배송_완료된_발주_건은_취소_시_예외를_던진다() {
        // given
        Order order = new OrderTestBuilder().build();
        order.startOrderProcess();
        order.ship();
        order.completeDelivery();

        // when & then
        assertThatThrownBy(order::cancel)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(OrderConstants.INVALID_CANCEL_STATUS_MESSAGE);
    }

    @Test
    void 이미_취소된_발주를_다시_취소_시도하면_예외를_던진다() {
        // given
        Order order = new OrderTestBuilder().build();
        order.cancel();

        // when & then
        assertThatThrownBy(order::cancel)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(OrderConstants.INVALID_CANCEL_STATUS_MESSAGE);
    }

    @Test
    void 발주_대기_상태에서_작업을_시작하면_작업_중_상태로_변경된다() {
        // given
        Order order = new OrderTestBuilder().build();

        // when
        order.startOrderProcess();

        // then
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.IN_PROGRESS);
    }

    @Test
    void 취소된_발주_건은_작업을_시작할_수_없으며_예외를_던진다() {
        // given
        Order order = new OrderTestBuilder().build();

        // when
        order.cancel();

        // then
        assertThatThrownBy(order::startOrderProcess)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(OrderConstants.INVALID_START_STATUS_MESSAGE);
    }

    @Test
    void 작업_중인_발주_건은_출고_처리를_통해_배송_중_상태로_변경된다() {
        // given
        Order order = new OrderTestBuilder().build();

        // when
        order.startOrderProcess();
        order.ship();

        // then
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.SHIPPED);
    }

    @Test
    void 배송_중인_발주_건은_배송_완료_상태로_변경될_수_있다() {
        // given
        Order order = new OrderTestBuilder().build();

        // when
        order.startOrderProcess();
        order.ship();
        order.completeDelivery();

        // then
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.DELIVERED);
    }

    @Test
    void 올바른_상태_순서를_위반하여_상태_전이를_시도하면_예외를_던진다() {
        // given
        Order order = new OrderTestBuilder().build();

        // when & then 1: PENDING -> SHIPPED 직접 시도
        assertThatThrownBy(order::ship)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(OrderConstants.INVALID_SHIP_STATUS_MESSAGE);

        // when & then 2: PENDING -> DELIVERED 직접 시도
        assertThatThrownBy(order::completeDelivery)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(OrderConstants.INVALID_DELIVERY_STATUS_MESSAGE);
    }
}
