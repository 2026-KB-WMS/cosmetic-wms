package com.kb.cosmetic_wms.outbound.adapter.out.external;

import com.kb.cosmetic_wms.inventory.application.port.in.FindLotAllocationUseCase;
import com.kb.cosmetic_wms.outbound.application.port.out.LotAllocationQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LotAllocationQueryAdapter implements LotAllocationQueryPort {

    private final FindLotAllocationUseCase findLotAllocationUseCase;

    @Override
    public List<Long> findAllocatedOutboundIds(Long lotId) {
        return findLotAllocationUseCase.findAllocatedOutboundIds(lotId);
    }
}
