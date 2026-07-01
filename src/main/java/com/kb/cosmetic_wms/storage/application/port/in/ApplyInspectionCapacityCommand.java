package com.kb.cosmetic_wms.storage.application.port.in;

public record ApplyInspectionCapacityCommand(
        Long warehouseId,
        Long productId,
        int passedQuantity,
        int failedQuantity
) {}
