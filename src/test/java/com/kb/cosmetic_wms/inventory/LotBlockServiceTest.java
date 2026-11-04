package com.kb.cosmetic_wms.inventory;

import com.kb.cosmetic_wms.inventory.application.port.out.LotBlockPort;
import com.kb.cosmetic_wms.inventory.application.service.LotBlockService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LotBlockServiceTest {

    @InjectMocks
    private LotBlockService service;

    @Mock
    private LotBlockPort lotBlockPort;

    @Test
    void 로트를_차단하면_사유와_함께_차단_목록에_반영한다() {
        service.block(10L, "RECALLED");

        verify(lotBlockPort).block(10L, "RECALLED");
    }

    @Test
    void 차단을_해제하면_차단_목록에서_제거한다() {
        service.release(10L);

        verify(lotBlockPort).release(10L);
    }
}
