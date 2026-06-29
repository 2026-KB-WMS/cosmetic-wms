package com.kb.cosmetic_wms.inspection.application.port.in;

import com.kb.cosmetic_wms.inspection.domain.enums.InspectionSourceType;

import java.time.LocalDate;

public record CreateInspectionCommand(
        InspectionSourceType sourceType,
        Long sourceId,
        int inspectionQuantity,
        Long productId,
        Long inboundId,
        String manufacturerLotNumber,
        Long warehouseId,
        LocalDate expiryDate
) {}
