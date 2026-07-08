package com.kb.cosmetic_wms.outbound;

import com.kb.cosmetic_wms.outbound.application.port.in.CreateOutboundFromAssignmentCommand;
import com.kb.cosmetic_wms.outbound.application.port.in.OutboundResult;
import com.kb.cosmetic_wms.outbound.application.port.out.FefoInventoryQueryPort;
import com.kb.cosmetic_wms.outbound.application.port.out.InventoryAllocationPort;
import com.kb.cosmetic_wms.outbound.application.port.out.OrderPreparationPort;
import com.kb.cosmetic_wms.outbound.application.port.out.OutboundPort;
import com.kb.cosmetic_wms.outbound.application.service.CreateOutboundFromAssignmentService;
import com.kb.cosmetic_wms.outbound.domain.enums.OutboundStatus;
import com.kb.cosmetic_wms.outbound.domain.exception.OutboundInsufficientStockException;
import com.kb.cosmetic_wms.outbound.domain.model.AvailableStock;
import com.kb.cosmetic_wms.outbound.domain.model.Outbound;
import com.kb.cosmetic_wms.outbound.domain.service.FefoAllocationSelector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateOutboundFromAssignmentServiceTest {

    private static final Long ORDER_ID = 1L;
    private static final Long WAREHOUSE_ID = 2L;
    private static final Long MEMBER_ID = 9L;
    private static final Long ORDER_ITEM_ID = 11L;
    private static final Long PRODUCT_ID = 100L;
    private static final Long SAVED_OUTBOUND_ID = 5L;

    @InjectMocks
    private CreateOutboundFromAssignmentService service;

    @Mock
    private OutboundPort outboundPort;

    @Mock
    private FefoInventoryQueryPort fefoInventoryQueryPort;

    @Mock
    private InventoryAllocationPort inventoryAllocationPort;

    @Mock
    private OrderPreparationPort orderPreparationPort;

    @Spy
    private FefoAllocationSelector fefoAllocationSelector = new FefoAllocationSelector();

    @BeforeEach
    void setUp() {
        lenient().when(outboundPort.save(any(Outbound.class))).thenAnswer(invocation -> {
            Outbound outbound = invocation.getArgument(0);
            if (outbound.getId() == null) {
                ReflectionTestUtils.setField(outbound, "id", SAVED_OUTBOUND_ID);
            }
            return outbound;
        });
    }

    private CreateOutboundFromAssignmentCommand command(int quantity) {
        return new CreateOutboundFromAssignmentCommand(ORDER_ID, WAREHOUSE_ID, MEMBER_ID,
                List.of(new CreateOutboundFromAssignmentCommand.ItemDemand(ORDER_ITEM_ID, PRODUCT_ID, quantity)));
    }

    @Test
    void 창고_배정을_받으면_FEFO_순서로_할당된_출고_전표가_생성되고_발주가_준비_상태로_전환된다() {
        given(fefoInventoryQueryPort.findAvailableForFefo(PRODUCT_ID, WAREHOUSE_ID))
                .willReturn(List.of(new AvailableStock(21L, 20), new AvailableStock(22L, 50)));

        OutboundResult result = service.createFromAssignment(command(30));

        assertThat(result.outboundStatus()).isEqualTo(OutboundStatus.ALLOCATED);
        verify(inventoryAllocationPort).allocate(21L, 20, SAVED_OUTBOUND_ID, MEMBER_ID);
        verify(inventoryAllocationPort).allocate(22L, 10, SAVED_OUTBOUND_ID, MEMBER_ID);
        verify(orderPreparationPort).startPreparation(ORDER_ID);
        verify(outboundPort, times(2)).save(any(Outbound.class));
    }

    @Test
    void 가용_재고가_부족하면_예외가_전파되고_출고_전표가_저장되지_않는다() {
        given(fefoInventoryQueryPort.findAvailableForFefo(PRODUCT_ID, WAREHOUSE_ID))
                .willReturn(List.of(new AvailableStock(21L, 10)));

        assertThatThrownBy(() -> service.createFromAssignment(command(30)))
                .isInstanceOf(OutboundInsufficientStockException.class);

        verify(outboundPort, never()).save(any(Outbound.class));
        verify(inventoryAllocationPort, never()).allocate(any(), anyInt(), any(), any());
        verify(orderPreparationPort, never()).startPreparation(any());
    }
}
