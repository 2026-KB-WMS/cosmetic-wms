package com.kb.cosmetic_wms.domain.inspection.fixture;

import com.kb.cosmetic_wms.domain.inspection.entity.QualityInspection;
import com.kb.cosmetic_wms.domain.inspection.enums.InspectionSourceType;

public class QualityInspectionTestBuilder {

    private InspectionSourceType sourceType = InspectionSourceType.INBOUND;
    private Long sourceId = 10L;
    private Long inventoryId = null;
    private Long productId = 1L;
    private Long lotId = 100L;
    private Long sectionId = 200L;
    private Long warehouseId = 300L;
    private int inspectionQuantity = 10;
    private Long inspectorId = 1L;
    private int passedQuantity = 10;
    private int failedQuantity = 0;
    private String defectReason = null;

    public QualityInspectionTestBuilder sourceType(InspectionSourceType sourceType) {
        this.sourceType = sourceType;
        return this;
    }

    public QualityInspectionTestBuilder sourceId(Long sourceId) {
        this.sourceId = sourceId;
        return this;
    }

    public QualityInspectionTestBuilder inventoryId(Long inventoryId) {
        this.inventoryId = inventoryId;
        return this;
    }

    public QualityInspectionTestBuilder productId(Long productId) {
        this.productId = productId;
        return this;
    }

    public QualityInspectionTestBuilder lotId(Long lotId) {
        this.lotId = lotId;
        return this;
    }

    public QualityInspectionTestBuilder sectionId(Long sectionId) {
        this.sectionId = sectionId;
        return this;
    }

    public QualityInspectionTestBuilder warehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
        return this;
    }

    public QualityInspectionTestBuilder inspectionQuantity(int inspectionQuantity) {
        this.inspectionQuantity = inspectionQuantity;
        return this;
    }

    public QualityInspectionTestBuilder inspectorId(Long inspectorId) {
        this.inspectorId = inspectorId;
        return this;
    }

    public QualityInspectionTestBuilder passedQuantity(int passedQuantity) {
        this.passedQuantity = passedQuantity;
        return this;
    }

    public QualityInspectionTestBuilder failedQuantity(int failedQuantity) {
        this.failedQuantity = failedQuantity;
        return this;
    }

    public QualityInspectionTestBuilder defectReason(String defectReason) {
        this.defectReason = defectReason;
        return this;
    }

    /** WAITING 상태 전표 생성 */
    public QualityInspection buildPending() {
        return QualityInspection.createPending(sourceType, sourceId, inventoryId, inspectionQuantity,
                productId, lotId, sectionId, warehouseId);
    }

    /** IN_PROGRESS 상태 전표 생성 */
    public QualityInspection buildInProgress() {
        QualityInspection inspection = buildPending();
        inspection.startInspection(inspectorId);
        return inspection;
    }

    /** COMPLETED 상태 전표 생성 */
    public QualityInspection buildCompleted() {
        QualityInspection inspection = buildInProgress();
        inspection.completeInspection(passedQuantity, failedQuantity, defectReason);
        return inspection;
    }
}
