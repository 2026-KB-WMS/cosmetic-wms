package com.kb.cosmetic_wms.domain.inspection.event;

import com.kb.cosmetic_wms.domain.inspection.entity.QualityInspection;
import com.kb.cosmetic_wms.domain.inspection.enums.InspectionSourceType;

public record InspectionCompletedEvent(
        Long inspectionId,
        InspectionSourceType sourceType,
        Long sourceId,
        Long inventoryId,
        int inspectionQuantity,
        int passedQuantity,
        int failedQuantity,
        String defectReason
) {
    public static InspectionCompletedEvent from(QualityInspection inspection) {
        return new InspectionCompletedEvent(
                inspection.getId(),
                inspection.getSourceType(),
                inspection.getSourceId(),
                inspection.getInventoryId(),
                inspection.getInspectionQuantity(),
                inspection.getPassedQuantity(),
                inspection.getFailedQuantity(),
                inspection.getDefectReason()
        );
    }
}
