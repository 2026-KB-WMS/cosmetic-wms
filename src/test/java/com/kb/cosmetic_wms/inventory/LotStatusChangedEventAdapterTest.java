package com.kb.cosmetic_wms.inventory;

import com.kb.cosmetic_wms.inventory.adapter.in.event.LotStatusChangedEventAdapter;
import com.kb.cosmetic_wms.inventory.application.port.in.ManageLotBlockUseCase;
import com.kb.cosmetic_wms.lot.domain.event.LotStatusChangedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LotStatusChangedEventAdapterTest {

    @InjectMocks
    private LotStatusChangedEventAdapter adapter;

    @Mock
    private ManageLotBlockUseCase manageLotBlockUseCase;

    @Test
    void 출고할_수_없는_상태가_되면_로트를_차단한다() {
        adapter.onLotStatusChanged(new LotStatusChangedEvent(10L, "RECALLED", false));

        verify(manageLotBlockUseCase).block(10L, "RECALLED");
        verify(manageLotBlockUseCase, never()).release(10L);
    }

    @Test
    void 정상으로_복구되면_차단을_해제한다() {
        adapter.onLotStatusChanged(new LotStatusChangedEvent(10L, "AVAILABLE", true));

        verify(manageLotBlockUseCase).release(10L);
        verify(manageLotBlockUseCase, never()).block(10L, "AVAILABLE");
    }
}
