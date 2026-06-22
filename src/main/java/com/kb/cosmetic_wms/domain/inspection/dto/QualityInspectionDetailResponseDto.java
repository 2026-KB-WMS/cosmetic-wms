package com.kb.cosmetic_wms.domain.inspection.dto;

import com.kb.cosmetic_wms.domain.inspection.entity.QualityInspection;
import com.kb.cosmetic_wms.domain.inspection.enums.InspectionSourceType;
import com.kb.cosmetic_wms.domain.inspection.enums.InspectionStatus;

public record QualityInspectionDetailResponseDto(
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
    public static QualityInspectionDetailResponseDto from(QualityInspection inspection) {
        return new QualityInspectionDetailResponseDto(
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
