package com.kb.cosmetic_wms.inspection.application.port.in;

import com.kb.cosmetic_wms.inspection.domain.enums.InspectionSourceType;

import java.time.LocalDate;

public record CreateInspectionCommand(
        InspectionSourceType sourceType,
        Long sourceId,
        Long inventoryId,
        int inspectionQuantity,
        Long productId,
        Long lotId,
        Long sectionId,
        Long warehouseId,
        LocalDate expiryDate
) {}