package com.kb.cosmetic_wms.domain.inspection.fixture;

import com.kb.cosmetic_wms.domain.inspection.entity.QualityInspection;
import com.kb.cosmetic_wms.domain.inspection.enums.InspectionResult;

public class QualityInspectionTestBuilder {
    private Long inboundItemId = 10L;
    private Long inventoryId = 100L;
    private Long inspectorId = 1L;
    private InspectionResult result = InspectionResult.PASSED;
    private String defectReason = null;

    public QualityInspectionTestBuilder inboundItemId(Long inboundItemId) {
        this.inboundItemId = inboundItemId;
        return this;
    }

    public QualityInspectionTestBuilder inventoryId(Long inventoryId) {
        this.inventoryId = inventoryId;
        return this;
    }

    public QualityInspectionTestBuilder inspectorId(Long inspectorId) {
        this.inspectorId = inspectorId;
        return this;
    }

    public QualityInspectionTestBuilder result(InspectionResult result) {
        this.result = result;
        return this;
    }

    public QualityInspectionTestBuilder defectReason(String defectReason) {
        this.defectReason = defectReason;
        return this;
    }

    public QualityInspectionTestBuilder failed(String defectReason) {
        this.result = InspectionResult.FAILED;
        this.defectReason = defectReason;
        return this;
    }

    public QualityInspection build() {
        return QualityInspection.create(
                this.inboundItemId,
                this.inventoryId,
                this.inspectorId,
                this.result,
                this.defectReason
        );
    }
}
