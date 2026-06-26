package com.kb.cosmetic_wms.inventory.application.port.in;

import com.kb.cosmetic_wms.inventory.domain.enums.AllocStatus;
import com.kb.cosmetic_wms.inventory.domain.enums.LocStatus;
import com.kb.cosmetic_wms.inventory.domain.enums.QualityStatus;
import com.kb.cosmetic_wms.inventory.domain.model.Inventory;

import java.time.LocalDate;

public record InventoryResult(
        Long id,
        Long productId,
        Long lotId,
        Long sectionId,
        Long warehouseId,
        int quantity,
        int availableQuantity,
        AllocStatus allocStatus,
        QualityStatus qualityStatus,
        LocStatus locStatus,
        LocalDate expiryDate
) {
    public static InventoryResult from(Inventory inventory) {
        return new InventoryResult(
                inventory.getId(),
                inventory.getProductId(),
                inventory.getLotId(),
                inventory.getSectionId(),
                inventory.getWarehouseId(),
                inventory.getQuantity(),
                inventory.getAvailableQuantity(),
                inventory.getStatusSet().allocStatus(),
                inventory.getStatusSet().qualityStatus(),
                inventory.getStatusSet().locStatus(),
                inventory.getExpiryDate()
        );
    }
}