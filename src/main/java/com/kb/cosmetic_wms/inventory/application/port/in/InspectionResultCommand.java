package com.kb.cosmetic_wms.inventory.application.port.in;

import java.time.LocalDate;

public record InspectionResultCommand(
        Long productId,
        Long lotId,
        Long sectionId,
        Long warehouseId,
        int passedQuantity,
        int failedQuantity,
        Long inspectionId,
        Long memberId,
        LocalDate expiryDate
) {
}