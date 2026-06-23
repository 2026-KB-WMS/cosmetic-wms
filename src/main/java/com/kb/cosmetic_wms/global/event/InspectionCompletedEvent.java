package com.kb.cosmetic_wms.global.event;

public record InspectionCompletedEvent(
        Long inspectionId,
        String sourceType,
        Long sourceId,
        Long productId,
        Long lotId,
        Long sectionId,
        Long warehouseId,
        int inspectionQuantity,
        int passedQuantity,
        int failedQuantity,
        String defectReason
) {}