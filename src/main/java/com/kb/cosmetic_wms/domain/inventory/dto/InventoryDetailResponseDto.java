package com.kb.cosmetic_wms.domain.inventory.dto;

import com.kb.cosmetic_wms.domain.inventory.entity.Inventory;
import com.kb.cosmetic_wms.domain.inventory.enums.AllocStatus;
import com.kb.cosmetic_wms.domain.inventory.enums.LocStatus;
import com.kb.cosmetic_wms.domain.inventory.enums.QualityStatus;

public record InventoryDetailResponseDto(
        Long id,
        Long productId,
        Long lotId,
        Long sectionId,
        Long warehouseId,
        int quantity,
        int availableQuantity,
        AllocStatus allocStatus,
        QualityStatus qualityStatus,
        LocStatus locStatus
) {
    public static InventoryDetailResponseDto from(Inventory inventory) {
        return new InventoryDetailResponseDto(
                inventory.getId(),
                inventory.getProductId(),
                inventory.getLotId(),
                inventory.getSectionId(),
                inventory.getWarehouseId(),
                inventory.getQuantity(),
                inventory.getAvailableQuantity(),
                inventory.getStatusSet().allocStatus(),
                inventory.getStatusSet().qualityStatus(),
                inventory.getStatusSet().locStatus()
        );
    }
}
