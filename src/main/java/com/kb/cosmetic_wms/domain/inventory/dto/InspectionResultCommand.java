package com.kb.cosmetic_wms.domain.inventory.dto;

public record InspectionResultCommand(
        Long productId,
        Long lotId,
        Long sectionId,
        Long warehouseId,
        int passedQuantity,
        int failedQuantity,
        Long inspectionId,
        Long memberId
) {}