package com.kb.cosmetic_wms.inventory.application.service;

import com.kb.cosmetic_wms.inventory.application.port.in.FindLotAllocationUseCase;
import com.kb.cosmetic_wms.inventory.application.port.out.InventoryTransactionPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LotAllocationQueryService implements FindLotAllocationUseCase {

    private final InventoryTransactionPort inventoryTransactionPort;

    @Override
    public List<Long> findAllocatedOutboundIds(Long lotId) {
        return inventoryTransactionPort.findAllocatedReferenceIdsByLot(lotId);
    }
}
