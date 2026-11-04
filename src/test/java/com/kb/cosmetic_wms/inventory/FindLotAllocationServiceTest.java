package com.kb.cosmetic_wms.inventory;

import com.kb.cosmetic_wms.inventory.application.port.out.InventoryTransactionPort;
import com.kb.cosmetic_wms.inventory.application.service.LotAllocationQueryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class FindLotAllocationServiceTest {

    @InjectMocks
    private LotAllocationQueryService service;

    @Mock
    private InventoryTransactionPort inventoryTransactionPort;

    @Test
    void 로트에_할당된_출고_ID를_이력_포트에서_조회해_반환한다() {
        given(inventoryTransactionPort.findAllocatedReferenceIdsByLot(10L)).willReturn(List.of(101L, 102L));

        assertThat(service.findAllocatedOutboundIds(10L)).containsExactly(101L, 102L);
    }

    @Test
    void 할당된_출고가_없으면_빈_목록을_반환한다() {
        given(inventoryTransactionPort.findAllocatedReferenceIdsByLot(10L)).willReturn(List.of());

        assertThat(service.findAllocatedOutboundIds(10L)).isEmpty();
    }
}
