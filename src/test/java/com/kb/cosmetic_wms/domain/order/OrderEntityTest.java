package com.kb.cosmetic_wms.domain.order;

import com.kb.cosmetic_wms.domain.order.entity.Orders;
import com.kb.cosmetic_wms.domain.order.enums.OrderStatus;
import com.kb.cosmetic_wms.domain.order.exception.*;
import com.kb.cosmetic_wms.domain.order.fixture.OrderTestBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class OrderEntityTest {

    @Test
    void 발주를_정상적인_값으로_생성하면_최초_상태는_PENDING_이어야_한다() {
        Orders order = new OrderTestBuilder().build();

        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getOrderItems().getFirst().getOrders()).isEqualTo(order);
    }

    @Test
    void 발주_생성_시_가맹점_정보가_누락되면_예외를_던진다() {
        assertThatThrownBy(() -> new OrderTestBuilder().storeId(null).build())
                .isInstanceOf(OrderStoreRequiredException.class);
    }

    @Test
    void 발주_생성_시_배정_창고_정보가_누락되면_예외를_던진다() {
        assertThatThrownBy(() -> new OrderTestBuilder().warehouseId(null).build())
                .isInstanceOf(OrderWarehouseRequiredException.class);
    }

    @Test
    void 발주_생성_시_발주_항목_리스트가_비어있으면_예외를_던진다() {
        assertThatThrownBy(() -> new OrderTestBuilder().emptyOrderLines().build())
                .isInstanceOf(OrderItemsRequiredException.class);
    }

    @Test
    void 발주_생성_시_발주_항목_리스트가_null이면_예외를_던진다() {
        assertThatThrownBy(() -> Orders.create(1L, 10L, null))
                .isInstanceOf(OrderItemsRequiredException.class);
    }

    @Test
    void 발주_대기_상태에서는_발주를_취소할_수_있다() {
        Orders order = new OrderTestBuilder().build();
        order.cancel();

        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELED);
    }

    @Test
    void 발주_확정_상태에서_취소하면_예외를_던진다() {
        Orders order = new OrderTestBuilder().build();
        order.confirm();

        assertThatThrownBy(order::cancel)
                .isInstanceOf(OrderCancelNotAllowedException.class);
    }

    @Test
    void 배송_중_상태에서_취소하면_예외를_던진다() {
        Orders order = new OrderTestBuilder().build();
        order.confirm();
        order.startPreparation();
        order.ship();

        assertThatThrownBy(order::cancel)
                .isInstanceOf(OrderCancelNotAllowedException.class);
    }

    @Test
    void 배송_완료_상태에서_취소하면_예외를_던진다() {
        Orders order = new OrderTestBuilder().build();
        order.confirm();
        order.startPreparation();
        order.ship();
        order.completeDelivery();

        assertThatThrownBy(order::cancel)
                .isInstanceOf(OrderCancelNotAllowedException.class);
    }

    @Test
    void 이미_취소된_발주를_다시_취소_시도하면_예외를_던진다() {
        Orders order = new OrderTestBuilder().build();
        order.cancel();

        assertThatThrownBy(order::cancel)
                .isInstanceOf(OrderCancelNotAllowedException.class);
    }

    @Test
    void 발주_대기_상태에서_확정하면_발주_확정_상태로_변경된다() {
        Orders order = new OrderTestBuilder().build();
        order.confirm();

        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void 발주_확정_상태에서_배송_준비를_시작하면_배송_준비_중_상태로_변경된다() {
        Orders order = new OrderTestBuilder().build();
        order.confirm();
        order.startPreparation();

        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PREPARING);
    }

    @Test
    void 취소된_발주는_확정할_수_없으며_예외를_던진다() {
        Orders order = new OrderTestBuilder().build();
        order.cancel();

        assertThatThrownBy(order::confirm)
                .isInstanceOf(OrderConfirmNotAllowedException.class);
    }

    @Test
    void PENDING_상태에서_배송_준비를_시작하면_예외를_던진다() {
        Orders order = new OrderTestBuilder().build();

        assertThatThrownBy(order::startPreparation)
                .isInstanceOf(OrderPreparationNotAllowedException.class);
    }

    @Test
    void 배송_준비_중_상태의_발주_건은_출고_처리를_통해_배송_중_상태로_변경된다() {
        Orders order = new OrderTestBuilder().build();
        order.confirm();
        order.startPreparation();
        order.ship();

        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.SHIPPED);
    }

    @Test
    void PENDING_상태에서_출하하면_예외를_던진다() {
        Orders order = new OrderTestBuilder().build();

        assertThatThrownBy(order::ship)
                .isInstanceOf(OrderShipNotAllowedException.class);
    }

    @Test
    void 배송_중인_발주_건은_배송_완료_상태로_변경될_수_있다() {
        Orders order = new OrderTestBuilder().build();
        order.confirm();
        order.startPreparation();
        order.ship();
        order.completeDelivery();

        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.DELIVERED);
    }

    @Test
    void PENDING_상태에서_배송_완료_처리하면_예외를_던진다() {
        Orders order = new OrderTestBuilder().build();

        assertThatThrownBy(order::completeDelivery)
                .isInstanceOf(OrderDeliveryCompleteNotAllowedException.class);
    }
}