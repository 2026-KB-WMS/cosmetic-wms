package com.kb.cosmetic_wms.oms;

import com.kb.cosmetic_wms.oms.adapter.in.event.RetryableWarehouseAssigner;
import com.kb.cosmetic_wms.oms.application.port.in.AssignWarehouseCommand;
import com.kb.cosmetic_wms.oms.application.port.in.AssignWarehouseUseCase;
import com.kb.cosmetic_wms.oms.application.port.out.AssignmentFailurePort;
import com.kb.cosmetic_wms.order.domain.event.OrderConfirmedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RetryableWarehouseAssignerTest {

    @Mock
    private AssignWarehouseUseCase assignWarehouseUseCase;

    @Mock
    private AssignmentFailurePort assignmentFailurePort;

    @InjectMocks
    private RetryableWarehouseAssigner retryableWarehouseAssigner;

    @Test
    void 발주_확정_이벤트를_창고_배정_커맨드로_변환해_유스케이스를_호출한다() {
        // given
        OrderConfirmedEvent event = new OrderConfirmedEvent(1L, 10L,
                List.of(new OrderConfirmedEvent.ItemSnapshot(5L, 100L, 30)));

        // when
        retryableWarehouseAssigner.assignWithRetry(event);

        // then
        ArgumentCaptor<AssignWarehouseCommand> captor = ArgumentCaptor.forClass(AssignWarehouseCommand.class);
        verify(assignWarehouseUseCase).assign(captor.capture());
        AssignWarehouseCommand command = captor.getValue();
        assertThat(command.orderId()).isEqualTo(1L);
        assertThat(command.storeId()).isEqualTo(10L);
        assertThat(command.items()).hasSize(1);
        assertThat(command.items().getFirst().orderItemId()).isEqualTo(5L);
        assertThat(command.items().getFirst().productId()).isEqualTo(100L);
        assertThat(command.items().getFirst().quantity()).isEqualTo(30);
    }

    @Test
    void 재시도_소진_시_실패_이벤트를_기록한다() {
        // given
        OrderConfirmedEvent event = new OrderConfirmedEvent(1L, 10L,
                List.of(new OrderConfirmedEvent.ItemSnapshot(5L, 100L, 30)));
        RuntimeException cause = new RuntimeException("Routing API timeout");

        // when
        retryableWarehouseAssigner.recover(cause, event);

        // then
        verify(assignmentFailurePort).save(1L, 10L, "Routing API timeout");
    }
}
