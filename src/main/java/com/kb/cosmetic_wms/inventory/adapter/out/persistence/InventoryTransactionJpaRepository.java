package com.kb.cosmetic_wms.inventory.adapter.out.persistence;

import com.kb.cosmetic_wms.inventory.domain.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;

import java.util.List;

interface InventoryTransactionJpaRepository extends JpaRepository<InventoryTransactionEntity, Long> {

    List<InventoryTransactionEntity> findByTransactionTypeAndReferenceId(
            TransactionType transactionType, Long referenceId);

    @Query(value = """
            SELECT DISTINCT t.reference_id
            FROM inventory_transaction t
            JOIN inventory i ON i.inventory_id = t.inventory_id
            WHERE t.transaction_type = 'ALLOCATE'
              AND t.reference_id IS NOT NULL
              AND i.lot_id = :lotId
              AND i.alloc_status = 'ALLOCATED'
            ORDER BY t.reference_id
            """, nativeQuery = true)
    List<Long> findAllocatedReferenceIdsByLot(@Param("lotId") Long lotId);
}