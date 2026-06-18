package com.kb.cosmetic_wms.domain.inventory.repository;

import com.kb.cosmetic_wms.domain.inventory.entity.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {
}
