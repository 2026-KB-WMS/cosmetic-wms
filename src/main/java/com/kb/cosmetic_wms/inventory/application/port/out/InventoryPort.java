package com.kb.cosmetic_wms.inventory.application.port.out;

import com.kb.cosmetic_wms.inventory.domain.model.Inventory;
import com.kb.cosmetic_wms.inventory.domain.model.InventoryStatusSet;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface InventoryPort {
    Optional<Inventory> findById(Long id);

    List<Inventory> findByLotId(Long lotId);

    List<Inventory> findByProductId(Long productId);

    Optional<Inventory> findByIdForUpdate(Long id);

    /**
     * @param excludeId 병합 대상에서 제외할 재고 ID (null이면 제외 없음)
     */
    Optional<Inventory> findMergeTargetForUpdate(Long productId, Long lotId, Long sectionId,
                                                 InventoryStatusSet statusSet, Long excludeId);

    List<FefoInventoryView> findAvailableForFefo(Long productId, Long warehouseId);

    List<ProductAvailabilityView> findAvailabilityByProducts(Collection<Long> productIds);

    Inventory save(Inventory inventory);

    void delete(Inventory inventory);

    record FefoInventoryView(Long inventoryId, int availableQuantity) {
    }

    record ProductAvailabilityView(
            Long warehouseId,
            Long productId,
            int availableQuantity,
            LocalDate earliestExpiryDate
    ) {
    }
}
