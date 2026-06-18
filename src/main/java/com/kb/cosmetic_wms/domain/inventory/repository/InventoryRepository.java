package com.kb.cosmetic_wms.domain.inventory.repository;

import com.kb.cosmetic_wms.domain.inventory.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    List<Inventory> findByLotId(Long lotId);

    List<Inventory> findByProductId(Long productId);
}
