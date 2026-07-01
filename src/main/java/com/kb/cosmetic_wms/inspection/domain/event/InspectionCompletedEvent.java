package com.kb.cosmetic_wms.inspection.domain.event;

import java.time.LocalDate;

public record InspectionCompletedEvent(
        Long inspectionId,
        String sourceType,
        Long sourceId,
        Long productId,
        Long lotId,
        Long warehouseId,
        int inspectionQuantity,
        int passedQuantity,
        int failedQuantity,
        String defectReason,
        LocalDate expiryDate
) {}
