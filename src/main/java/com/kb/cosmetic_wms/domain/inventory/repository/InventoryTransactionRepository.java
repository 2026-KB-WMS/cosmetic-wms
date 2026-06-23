package com.kb.cosmetic_wms.domain.inventory.repository;

import com.kb.cosmetic_wms.domain.inventory.entity.InventoryTransaction;
import com.kb.cosmetic_wms.domain.inventory.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {

    List<InventoryTransaction> findByTransactionTypeAndReferenceId(TransactionType transactionType, Long referenceId);
}
