package com.kb.cosmetic_wms.inspection.adapter.in.web;

import com.kb.cosmetic_wms.inspection.application.port.in.InspectionResult;
import com.kb.cosmetic_wms.inspection.domain.enums.InspectionSourceType;
import com.kb.cosmetic_wms.inspection.domain.enums.InspectionStatus;

public record InspectionDetailResponse(
        Long id,
        InspectionSourceType sourceType,
        Long sourceId,
        Long lotId,
        Long inspectorId,
        InspectionStatus status,
        int inspectionQuantity,
        int passedQuantity,
        int failedQuantity,
        String defectReason
) {
    public static InspectionDetailResponse from(InspectionResult result) {
        return new InspectionDetailResponse(
                result.id(),
                result.sourceType(),
                result.sourceId(),
                result.lotId(),
                result.inspectorId(),
                result.status(),
                result.inspectionQuantity(),
                result.passedQuantity(),
                result.failedQuantity(),
                result.defectReason()
        );
    }
}
