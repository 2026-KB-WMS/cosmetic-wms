package com.kb.cosmetic_wms.inspection.application.port.in;

import com.kb.cosmetic_wms.inspection.domain.enums.InspectionSourceType;
import com.kb.cosmetic_wms.inspection.domain.enums.InspectionStatus;
import com.kb.cosmetic_wms.inspection.domain.model.QualityInspection;

public record InspectionResult(
        Long id,
        InspectionSourceType sourceType,
        Long sourceId,
        Long inventoryId,
        Long inspectorId,
        InspectionStatus status,
        int inspectionQuantity,
        int passedQuantity,
        int failedQuantity,
        String defectReason
) {
    public static InspectionResult from(QualityInspection inspection) {
        return new InspectionResult(
                inspection.getId(),
                inspection.getSourceType(),
                inspection.getSourceId(),
                inspection.getInventoryId(),
                inspection.getInspectorId(),
                inspection.getStatus(),
                inspection.getInspectionQuantity(),
                inspection.getPassedQuantity(),
                inspection.getFailedQuantity(),
                inspection.getDefectReason()
        );
    }
}