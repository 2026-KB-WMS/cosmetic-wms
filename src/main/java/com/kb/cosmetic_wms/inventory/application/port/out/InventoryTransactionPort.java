package com.kb.cosmetic_wms.inventory.application.port.out;

import com.kb.cosmetic_wms.inventory.domain.enums.TransactionType;
import com.kb.cosmetic_wms.inventory.domain.model.InventoryTransaction;

import java.util.List;

public interface InventoryTransactionPort {
    InventoryTransaction save(InventoryTransaction transaction);
    List<InventoryTransaction> findByTransactionTypeAndReferenceId(TransactionType type, Long referenceId);

    /**
     * 해당 로트의 현재 할당(ALLOCATED) 재고에 남아 있는 ALLOCATE 이력의 참조 ID(출고 ID)를 오름차순, 중복 없이 반환한다.
     */
    List<Long> findAllocatedReferenceIdsByLot(Long lotId);
}