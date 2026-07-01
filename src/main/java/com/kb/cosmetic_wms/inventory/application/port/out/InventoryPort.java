package com.kb.cosmetic_wms.inventory.application.port.out;

import com.kb.cosmetic_wms.inventory.application.port.in.FefoInventorySlice;
import com.kb.cosmetic_wms.inventory.domain.model.Inventory;
import com.kb.cosmetic_wms.inventory.domain.model.InventoryStatusSet;

import java.util.List;
import java.util.Optional;

public interface InventoryPort {
    Optional<Inventory> findById(Long id);
    List<Inventory> findByLotId(Long lotId);
    List<Inventory> findByProductId(Long productId);
    Optional<Inventory> findByIdForUpdate(Long id);
    Optional<Inventory> findMergeTargetForUpdate(Long productId, Long lotId, Long sectionId,
                                                  InventoryStatusSet statusSet, Long excludeId);
    List<FefoInventorySlice> findAvailableForFefo(Long productId, Long warehouseId);
    Inventory save(Inventory inventory);
    void delete(Inventory inventory);
}