package com.kb.cosmetic_wms.outbound;

import com.kb.cosmetic_wms.inventory.application.port.in.FindLotAllocationUseCase;
import com.kb.cosmetic_wms.outbound.adapter.out.external.LotAllocationQueryAdapter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class LotAllocationQueryAdapterTest {

    @InjectMocks
    private LotAllocationQueryAdapter adapter;

    @Mock
    private FindLotAllocationUseCase findLotAllocationUseCase;

    @Test
    void inventory가_알려준_로트의_할당_출고_ID를_그대로_전달한다() {
        given(findLotAllocationUseCase.findAllocatedOutboundIds(10L)).willReturn(List.of(101L, 102L));

        assertThat(adapter.findAllocatedOutboundIds(10L)).containsExactly(101L, 102L);
    }
}
