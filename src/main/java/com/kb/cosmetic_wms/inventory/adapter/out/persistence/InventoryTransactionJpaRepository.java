package com.kb.cosmetic_wms.inventory.adapter.out.persistence;

import com.kb.cosmetic_wms.inventory.domain.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface InventoryTransactionJpaRepository extends JpaRepository<InventoryTransactionEntity, Long> {

    List<InventoryTransactionEntity> findByTransactionTypeAndReferenceId(
            TransactionType transactionType, Long referenceId);
}