package com.kb.cosmetic_wms.putaway.application.port.in;

public record CreatePutawayOrderCommand(
        Long inspectionId,
        Long lotId,
        Long productId,
        Long warehouseId,
        int passedQuantity,
        int failedQuantity
) {}
