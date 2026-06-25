package com.kb.cosmetic_wms.inventory.application.port.out;

import com.kb.cosmetic_wms.inventory.domain.enums.TransactionType;
import com.kb.cosmetic_wms.inventory.domain.model.InventoryTransaction;

import java.util.List;

public interface InventoryTransactionPort {
    InventoryTransaction save(InventoryTransaction transaction);
    List<InventoryTransaction> findByTransactionTypeAndReferenceId(TransactionType type, Long referenceId);
}