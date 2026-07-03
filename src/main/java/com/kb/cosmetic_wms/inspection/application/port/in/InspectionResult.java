package com.kb.cosmetic_wms.inspection.application.port.in;

import com.kb.cosmetic_wms.inspection.domain.enums.InspectionSourceType;
import com.kb.cosmetic_wms.inspection.domain.enums.InspectionStatus;
import com.kb.cosmetic_wms.inspection.domain.model.Inspection;

public record InspectionResult(
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
    public static InspectionResult from(Inspection inspection) {
        return new InspectionResult(
                inspection.getId(),
                inspection.getSourceType(),
                inspection.getSourceId(),
                inspection.getLotId(),
                inspection.getInspectorId(),
                inspection.getStatus(),
                inspection.getInspectionQuantity(),
                inspection.getPassedQuantity(),
                inspection.getFailedQuantity(),
                inspection.getDefectReason()
        );
    }
}
