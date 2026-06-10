package com.kb.cosmetic_wms.domain.inspection.entity;

import com.kb.cosmetic_wms.domain.inspection.constants.QualityConstants;
import com.kb.cosmetic_wms.domain.inspection.enums.InspectionResult;
import com.kb.cosmetic_wms.global.common.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class QualityInspection extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long inboundItemId;
    private Long inventoryId;
    private Long inspectorId;
    private InspectionResult result;
    private String defectReason;

    @Builder(access = AccessLevel.PRIVATE)
    private QualityInspection(Long inboundItemId, Long inventoryId, Long inspectorId,
                              InspectionResult result, String defectReason) {
        this.inboundItemId = inboundItemId;
        this.inventoryId = inventoryId;
        this.inspectorId = inspectorId;
        this.result = result;
        this.defectReason = defectReason;
    }

    public static QualityInspection create(Long inboundItemId, Long inventoryId, Long inspectorId,
                                           InspectionResult result, String defectReason) {
        validate(inboundItemId, inventoryId, inspectorId, result, defectReason);

        return QualityInspection.builder()
                .inboundItemId(inboundItemId)
                .inventoryId(inventoryId)
                .inspectorId(inspectorId)
                .result(result)
                .defectReason(result == InspectionResult.PASSED ? null : (defectReason != null ? defectReason.trim() : null))
                .build();
    }

    private static void validate(Long inboundItemId, Long inventoryId, Long inspectorId,
                                 InspectionResult result, String defectReason) {
        if (inboundItemId == null) {
            throw new IllegalArgumentException(QualityConstants.INBOUND_ITEM_ID_REQUIRED_MESSAGE);
        }
        if (inventoryId == null) {
            throw new IllegalArgumentException(QualityConstants.INVENTORY_ID_REQUIRED_MESSAGE);
        }
        if (inspectorId == null) {
            throw new IllegalArgumentException(QualityConstants.INSPECTOR_ID_REQUIRED_MESSAGE);
        }
        if (result == null) {
            throw new IllegalArgumentException(QualityConstants.INSPECTION_RESULT_REQUIRED_MESSAGE);
        }

        if (result == InspectionResult.FAILED && (defectReason == null || defectReason.isBlank())) {
            throw new IllegalArgumentException(QualityConstants.DEFECT_REASON_REQUIRED_MESSAGE);
        }
    }
}
