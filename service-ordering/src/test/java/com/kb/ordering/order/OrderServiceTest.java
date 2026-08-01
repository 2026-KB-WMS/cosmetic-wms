package com.kb.ordering.order;

import com.kb.ordering.order.application.port.in.CreateOrderCommand;
import com.kb.ordering.order.application.port.in.OrderResult;
import com.kb.ordering.order.application.port.out.EventPublisher;
import com.kb.ordering.order.application.port.out.OrderPort;
import com.kb.ordering.order.application.service.OrderService;
import com.kb.ordering.order.domain.event.OrderConfirmedEvent;
import com.kb.ordering.order.domain.enums.OrderStatus;
import com.kb.ordering.order.domain.exception.*;
import com.kb.ordering.order.domain.model.Order;
import com.kb.ordering.order.fixture.OrderTestBuilder;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderPort orderPort;

    @Mock
    private EventPublisher eventPublisher;

    @Nested
    class 발주_신청 {

        @Test
        void 올바른_발주_정보가_주어지면_창고_미배정_상태의_발주_신청_전표가_성공적으로_생성된다() {
            // given
            CreateOrderCommand command = new CreateOrderCommand(
                    1L, List.of(new CreateOrderCommand.OrderLineCommand(1L, 10))
            );
            Order savedOrder = new OrderTestBuilder().build();
            ReflectionTestUtils.setField(savedOrder, "id", 1L);
            given(orderPort.save(any(Order.class))).willReturn(savedOrder);

            // when
            OrderResult response = orderService.createOrder(command);

            // then
            assertThat(response.orderStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(response.storeId()).isEqualTo(1L);
            assertThat(response.warehouseId()).isNull();
        }

        @Test
        void 발주_신청_시_가맹점_정보가_누락되면_예외가_발생한다() {
            CreateOrderCommand command = new CreateOrderCommand(
                    null, List.of(new CreateOrderCommand.OrderLineCommand(1L, 10))
            );

            assertThatThrownBy(() -> orderService.createOrder(command))
                    .isInstanceOf(OrderStoreRequiredException.class);
        }

        @Test
        void 발주_신청_시_발주_품목_목록이_비어있으면_예외가_발생한다() {
            CreateOrderCommand command = new CreateOrderCommand(1L, List.of());

            assertThatThrownBy(() -> orderService.createOrder(command))
                    .isInstanceOf(OrderItemsRequiredException.class);
        }
    }

    @Nested
    class 발주_확정 {

        @Test
        void 발주_신청_상태의_전표를_확정하면_발주_확정_상태로_정상_전환된다() {
            // given
            Long orderId = 1L;
            Order order = new OrderTestBuilder().build();
            given(orderPort.findByIdWithItemsForUpdate(orderId)).willReturn(Optional.of(order));
            given(orderPort.save(any(Order.class))).willAnswer(inv -> inv.getArgument(0));

            // when
            OrderResult response = orderService.confirmOrder(orderId);

            // then
            assertThat(response.orderStatus()).isEqualTo(OrderStatus.CONFIRMED);
        }

        @Test
        void 이미_발주_확정되었거나_취소된_전표에_대해_다시_확정을_요청하면_예외가_발생한다() {
            // given
            Long orderId = 1L;
            Order confirmedOrder = new OrderTestBuilder().build();
            confirmedOrder.confirm();
            given(orderPort.findByIdWithItemsForUpdate(orderId)).willReturn(Optional.of(confirmedOrder));

            // when & then
            assertThatThrownBy(() -> orderService.confirmOrder(orderId))
                    .isInstanceOf(OrderConfirmNotAllowedException.class);
        }

        @Test
        void 존재하지_않는_발주_ID로_확정을_요청하면_예외가_발생한다() {
            // given
            Long nonExistentOrderId = 999L;
            given(orderPort.findByIdWithItemsForUpdate(nonExistentOrderId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> orderService.confirmOrder(nonExistentOrderId))
                    .isInstanceOf(OrderNotFoundException.class);
        }

        @Test
        void 발주_확정_성공_시_발주_확정_이벤트가_정확히_1번_발행된다() {
            // given
            Long orderId = 1L;
            Order order = new OrderTestBuilder().build();
            given(orderPort.findByIdWithItemsForUpdate(orderId)).willReturn(Optional.of(order));
            given(orderPort.save(any(Order.class))).willAnswer(inv -> inv.getArgument(0));

            // when
            orderService.confirmOrder(orderId);

            // then
            then(eventPublisher).should(times(1)).publishOrderConfirmed(any(OrderConfirmedEvent.class));
        }

        @Test
        void 발주_확정_이벤트에는_발주_ID와_가맹점_ID와_품목_스냅샷이_포함된다() {
            // given
            Long orderId = 1L;
            Order order = new OrderTestBuilder().build();
            ReflectionTestUtils.setField(order, "id", orderId);
            given(orderPort.findByIdWithItemsForUpdate(orderId)).willReturn(Optional.of(order));
            given(orderPort.save(any(Order.class))).willAnswer(inv -> inv.getArgument(0));

            ArgumentCaptor<OrderConfirmedEvent> captor = ArgumentCaptor.forClass(OrderConfirmedEvent.class);

            // when
            orderService.confirmOrder(orderId);

            // then
            then(eventPublisher).should().publishOrderConfirmed(captor.capture());
            OrderConfirmedEvent event = captor.getValue();
            assertThat(event.orderId()).isEqualTo(orderId);
            assertThat(event.storeId()).isEqualTo(order.getStoreId());
            assertThat(event.items()).hasSize(order.getOrderItems().size());
        }
    }

    @Nested
    class 창고_배정 {

        @Test
        void 발주_확정_상태의_전표에_창고를_배정하면_창고_ID가_반영된다() {
            // given
            Long orderId = 1L;
            Order order = new OrderTestBuilder().build();
            order.confirm();
            given(orderPort.findByIdForUpdate(orderId)).willReturn(Optional.of(order));
            given(orderPort.save(any(Order.class))).willAnswer(inv -> inv.getArgument(0));

            // when
            OrderResult response = orderService.assignWarehouse(orderId, 10L);

            // then
            assertThat(response.warehouseId()).isEqualTo(10L);
            assertThat(response.orderStatus()).isEqualTo(OrderStatus.CONFIRMED);
        }

        @Test
        void 발주_확정_이외의_상태에서_창고를_배정하면_예외가_발생한다() {
            // given
            Long orderId = 1L;
            Order order = new OrderTestBuilder().build(); // PENDING 상태
            given(orderPort.findByIdForUpdate(orderId)).willReturn(Optional.of(order));

            // when & then
            assertThatThrownBy(() -> orderService.assignWarehouse(orderId, 10L))
                    .isInstanceOf(OrderWarehouseAssignNotAllowedException.class);
        }
    }

    @Nested
    class 배송_준비_시작 {

        @Test
        void 발주_확정_상태의_전표는_물류센터_작업_시작_시_배송_준비_중_상태로_정상_전환된다() {
            // given
            Long orderId = 1L;
            Order order = new OrderTestBuilder().build();
            order.confirm();
            given(orderPort.findByIdForUpdate(orderId)).willReturn(Optional.of(order));
            given(orderPort.save(any(Order.class))).willAnswer(inv -> inv.getArgument(0));

            // when
            OrderResult response = orderService.startPreparation(orderId);

            // then
            assertThat(response.orderStatus()).isEqualTo(OrderStatus.PREPARING);
        }

        @Test
        void 발주_확정_이외의_상태인_전표에_대해_배송_준비_시작을_요청하면_예외가_발생한다() {
            // given
            Long orderId = 1L;
            Order order = new OrderTestBuilder().build(); // PENDING 상태
            given(orderPort.findByIdForUpdate(orderId)).willReturn(Optional.of(order));

            // when & then
            assertThatThrownBy(() -> orderService.startPreparation(orderId))
                    .isInstanceOf(OrderPreparationNotAllowedException.class);
        }
    }

    @Nested
    class 배송_출하 {

        @Test
        void 배송_준비_중_상태의_전표는_출고_완료_시_배송_중_상태로_정상_전환된다() {
            // given
            Long orderId = 1L;
            Order order = new OrderTestBuilder().build();
            order.confirm();
            order.startPreparation();
            given(orderPort.findByIdForUpdate(orderId)).willReturn(Optional.of(order));
            given(orderPort.save(any(Order.class))).willAnswer(inv -> inv.getArgument(0));

            // when
            OrderResult response = orderService.ship(orderId);

            // then
            assertThat(response.orderStatus()).isEqualTo(OrderStatus.SHIPPED);
        }

        @Test
        void 배송_준비_중_이외의_상태인_전표에_대해_배송_출하를_요청하면_예외가_발생한다() {
            // given
            Long orderId = 1L;
            Order order = new OrderTestBuilder().build();
            order.confirm(); // CONFIRMED, not PREPARING
            given(orderPort.findByIdForUpdate(orderId)).willReturn(Optional.of(order));

            // when & then
            assertThatThrownBy(() -> orderService.ship(orderId))
                    .isInstanceOf(OrderShipNotAllowedException.class);
        }
    }

    @Nested
    class 배송_완료 {

        @Test
        void 배송_중_상태의_전표는_가맹점_인입_시_배송_완료_상태로_정상_전환된다() {
            // given
            Long orderId = 1L;
            Order order = new OrderTestBuilder().build();
            order.confirm();
            order.startPreparation();
            order.ship();
            given(orderPort.findByIdForUpdate(orderId)).willReturn(Optional.of(order));
            given(orderPort.save(any(Order.class))).willAnswer(inv -> inv.getArgument(0));

            // when
            OrderResult response = orderService.completeDelivery(orderId);

            // then
            assertThat(response.orderStatus()).isEqualTo(OrderStatus.DELIVERED);
        }

        @Test
        void 배송_중_이외의_상태인_전표에_대해_배송_완료를_요청하면_예외가_발생한다() {
            // given
            Long orderId = 1L;
            Order order = new OrderTestBuilder().build();
            order.confirm();
            order.startPreparation(); // PREPARING, not SHIPPED
            given(orderPort.findByIdForUpdate(orderId)).willReturn(Optional.of(order));

            // when & then
            assertThatThrownBy(() -> orderService.completeDelivery(orderId))
                    .isInstanceOf(OrderDeliveryCompleteNotAllowedException.class);
        }
    }

    @Nested
    class 발주_취소 {

        @Test
        void 발주_신청_상태의_발주는_취소가_가능하며_발주_취소_상태로_정상_전환된다() {
            // given
            Long orderId = 1L;
            Order order = new OrderTestBuilder().build();
            given(orderPort.findByIdForUpdate(orderId)).willReturn(Optional.of(order));
            given(orderPort.save(any(Order.class))).willAnswer(inv -> inv.getArgument(0));

            // when
            OrderResult response = orderService.cancelOrder(orderId);

            // then
            assertThat(response.orderStatus()).isEqualTo(OrderStatus.CANCELED);
        }

        @Test
        void 발주_확정_이후_단계의_전표는_취소할_수_없고_예외가_발생한다() {
            // given
            Long orderId = 1L;
            Order order = new OrderTestBuilder().build();
            order.confirm();
            given(orderPort.findByIdForUpdate(orderId)).willReturn(Optional.of(order));

            // when & then
            assertThatThrownBy(() -> orderService.cancelOrder(orderId))
                    .isInstanceOf(OrderCancelNotAllowedException.class);
        }

        @Test
        void 이미_발주_취소된_전표에_대해_중복으로_취소를_요청하면_예외가_발생한다() {
            // given
            Long orderId = 1L;
            Order order = new OrderTestBuilder().build();
            order.cancel();
            given(orderPort.findByIdForUpdate(orderId)).willReturn(Optional.of(order));

            // when & then
            assertThatThrownBy(() -> orderService.cancelOrder(orderId))
                    .isInstanceOf(OrderCancelNotAllowedException.class);
        }

        // TODO: Kafka 도입 후 이벤트 미발행 검증 테스트 복원
        // @Test void 발주_취소_성공_시_발주_확정_이벤트가_절대_발행되지_않는다() { ... }
    }
}