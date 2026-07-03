package com.kb.cosmetic_wms.order;

import com.kb.cosmetic_wms.order.domain.enums.OrderStatus;
import com.kb.cosmetic_wms.order.domain.exception.*;
import com.kb.cosmetic_wms.order.domain.model.Order;
import com.kb.cosmetic_wms.order.fixture.OrderTestBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class OrderEntityTest {

    @Test
    void 발주를_정상적인_값으로_생성하면_최초_상태는_PENDING_이고_창고는_미배정_상태여야_한다() {
        Order order = new OrderTestBuilder().build();

        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getWarehouseId()).isNull();
        assertThat(order.getOrderItems()).hasSize(1);
        assertThat(order.getOrderItems().getFirst().getProductId()).isEqualTo(1L);
        assertThat(order.getOrderItems().getFirst().getQuantity()).isEqualTo(10);
    }

    @Test
    void 발주_생성_시_가맹점_정보가_누락되면_예외를_던진다() {
        assertThatThrownBy(() -> new OrderTestBuilder().storeId(null).build())
                .isInstanceOf(OrderStoreRequiredException.class);
    }

    @Test
    void 발주_생성_시_발주_항목_리스트가_비어있으면_예외를_던진다() {
        assertThatThrownBy(() -> new OrderTestBuilder().emptyOrderLines().build())
                .isInstanceOf(OrderItemsRequiredException.class);
    }

    @Test
    void 발주_생성_시_발주_항목_리스트가_null이면_예외를_던진다() {
        assertThatThrownBy(() -> Order.create(1L, null))
                .isInstanceOf(OrderItemsRequiredException.class);
    }

    @Test
    void 발주_확정_상태에서_창고를_배정하면_창고_ID가_확정된다() {
        Order order = new OrderTestBuilder().build();
        order.confirm();

        order.assignWarehouse(10L);

        assertThat(order.getWarehouseId()).isEqualTo(10L);
    }

    @Test
    void 창고_배정_시_창고_ID가_null이면_예외를_던진다() {
        Order order = new OrderTestBuilder().build();
        order.confirm();

        assertThatThrownBy(() -> order.assignWarehouse(null))
                .isInstanceOf(OrderWarehouseRequiredException.class);
    }

    @Test
    void 발주_대기_상태에서_창고를_배정하면_예외를_던진다() {
        Order order = new OrderTestBuilder().build();

        assertThatThrownBy(() -> order.assignWarehouse(10L))
                .isInstanceOf(OrderWarehouseAssignNotAllowedException.class);
    }

    @Test
    void 이미_창고가_배정된_발주에_다시_배정하면_예외를_던진다() {
        Order order = new OrderTestBuilder().build();
        order.confirm();
        order.assignWarehouse(10L);

        assertThatThrownBy(() -> order.assignWarehouse(20L))
                .isInstanceOf(OrderWarehouseAssignNotAllowedException.class);
    }

    @Test
    void 발주_대기_상태에서는_발주를_취소할_수_있다() {
        Order order = new OrderTestBuilder().build();
        order.cancel();

        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELED);
    }

    @Test
    void 발주_확정_상태에서_취소하면_예외를_던진다() {
        Order order = new OrderTestBuilder().build();
        order.confirm();

        assertThatThrownBy(order::cancel)
                .isInstanceOf(OrderCancelNotAllowedException.class);
    }

    @Test
    void 배송_중_상태에서_취소하면_예외를_던진다() {
        Order order = new OrderTestBuilder().build();
        order.confirm();
        order.startPreparation();
        order.ship();

        assertThatThrownBy(order::cancel)
                .isInstanceOf(OrderCancelNotAllowedException.class);
    }

    @Test
    void 배송_완료_상태에서_취소하면_예외를_던진다() {
        Order order = new OrderTestBuilder().build();
        order.confirm();
        order.startPreparation();
        order.ship();
        order.completeDelivery();

        assertThatThrownBy(order::cancel)
                .isInstanceOf(OrderCancelNotAllowedException.class);
    }

    @Test
    void 이미_취소된_발주를_다시_취소_시도하면_예외를_던진다() {
        Order order = new OrderTestBuilder().build();
        order.cancel();

        assertThatThrownBy(order::cancel)
                .isInstanceOf(OrderCancelNotAllowedException.class);
    }

    @Test
    void 발주_대기_상태에서_확정하면_발주_확정_상태로_변경된다() {
        Order order = new OrderTestBuilder().build();
        order.confirm();

        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void 발주_확정_상태에서_배송_준비를_시작하면_배송_준비_중_상태로_변경된다() {
        Order order = new OrderTestBuilder().build();
        order.confirm();
        order.startPreparation();

        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PREPARING);
    }

    @Test
    void 취소된_발주는_확정할_수_없으며_예외를_던진다() {
        Order order = new OrderTestBuilder().build();
        order.cancel();

        assertThatThrownBy(order::confirm)
                .isInstanceOf(OrderConfirmNotAllowedException.class);
    }

    @Test
    void PENDING_상태에서_배송_준비를_시작하면_예외를_던진다() {
        Order order = new OrderTestBuilder().build();

        assertThatThrownBy(order::startPreparation)
                .isInstanceOf(OrderPreparationNotAllowedException.class);
    }

    @Test
    void 배송_준비_중_상태의_발주_건은_출고_처리를_통해_배송_중_상태로_변경된다() {
        Order order = new OrderTestBuilder().build();
        order.confirm();
        order.startPreparation();
        order.ship();

        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.SHIPPED);
    }

    @Test
    void PENDING_상태에서_출하하면_예외를_던진다() {
        Order order = new OrderTestBuilder().build();

        assertThatThrownBy(order::ship)
                .isInstanceOf(OrderShipNotAllowedException.class);
    }

    @Test
    void 배송_중인_발주_건은_배송_완료_상태로_변경될_수_있다() {
        Order order = new OrderTestBuilder().build();
        order.confirm();
        order.startPreparation();
        order.ship();
        order.completeDelivery();

        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.DELIVERED);
    }

    @Test
    void PENDING_상태에서_배송_완료_처리하면_예외를_던진다() {
        Order order = new OrderTestBuilder().build();

        assertThatThrownBy(order::completeDelivery)
                .isInstanceOf(OrderDeliveryCompleteNotAllowedException.class);
    }
}
