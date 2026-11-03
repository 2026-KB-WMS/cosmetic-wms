package com.kb.cosmetic_wms.order;

import com.kb.cosmetic_wms.order.application.port.in.OrderAssignmentTarget;
import com.kb.cosmetic_wms.order.application.port.out.OrderPort;
import com.kb.cosmetic_wms.order.application.service.OrderService;
import com.kb.cosmetic_wms.order.domain.model.Order;
import com.kb.cosmetic_wms.order.fixture.OrderTestBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class FindOrderAssignmentTargetServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderPort orderPort;

    @Test
    void 확정된_미배정_발주는_배정_대기로_품목과_함께_조회된다() {
        // given
        Order order = new OrderTestBuilder().storeId(10L).addOrderLine(100L, 30).build();
        order.confirm();
        given(orderPort.findByIdWithItems(1L)).willReturn(Optional.of(order));

        // when
        Optional<OrderAssignmentTarget> result = orderService.findAssignmentTarget(1L);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().storeId()).isEqualTo(10L);
        assertThat(result.get().awaitingAssignment()).isTrue();
        assertThat(result.get().items()).extracting(OrderAssignmentTarget.Item::productId).contains(100L);
    }

    @Test
    void 이미_창고가_배정된_발주는_배정_대기가_아니다() {
        // given
        Order order = new OrderTestBuilder().build();
        order.confirm();
        order.assignWarehouse(5L);
        given(orderPort.findByIdWithItems(1L)).willReturn(Optional.of(order));

        // when & then
        assertThat(orderService.findAssignmentTarget(1L).orElseThrow().awaitingAssignment()).isFalse();
    }

    @Test
    void 존재하지_않는_발주는_빈_결과를_반환한다() {
        given(orderPort.findByIdWithItems(999L)).willReturn(Optional.empty());

        assertThat(orderService.findAssignmentTarget(999L)).isEmpty();
    }
}
