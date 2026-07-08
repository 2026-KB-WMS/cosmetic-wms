package com.kb.cosmetic_wms.inventory.application.port.in;

import java.time.LocalDate;

public record InspectionResultCommand(
        Long productId,
        Long lotId,
        Long warehouseId,
        int passedQuantity,
        int failedQuantity,
        Long inspectionId,
        Long memberId,
        LocalDate expiryDate
) {}