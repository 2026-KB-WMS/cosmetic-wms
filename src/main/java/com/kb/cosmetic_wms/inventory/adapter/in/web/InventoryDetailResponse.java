package com.kb.cosmetic_wms.inventory.adapter.in.web;

import com.kb.cosmetic_wms.inventory.application.port.in.InventoryResult;
import com.kb.cosmetic_wms.inventory.domain.enums.AllocStatus;
import com.kb.cosmetic_wms.inventory.domain.enums.LocStatus;
import com.kb.cosmetic_wms.inventory.domain.enums.QualityStatus;

import java.time.LocalDate;

public record InventoryDetailResponse(
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
    public static InventoryDetailResponse from(InventoryResult result) {
        return new InventoryDetailResponse(
                result.id(),
                result.productId(),
                result.lotId(),
                result.sectionId(),
                result.warehouseId(),
                result.quantity(),
                result.availableQuantity(),
                result.allocStatus(),
                result.qualityStatus(),
                result.locStatus(),
                result.expiryDate()
        );
    }
}