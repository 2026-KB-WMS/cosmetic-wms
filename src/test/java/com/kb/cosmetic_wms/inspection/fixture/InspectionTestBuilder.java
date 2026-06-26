package com.kb.cosmetic_wms.inspection.fixture;

import com.kb.cosmetic_wms.inspection.domain.enums.InspectionSourceType;
import com.kb.cosmetic_wms.inspection.domain.model.Inspection;

import java.time.LocalDate;

public class InspectionTestBuilder {

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

    public InspectionTestBuilder sourceType(InspectionSourceType sourceType) {
        this.sourceType = sourceType;
        return this;
    }

    public InspectionTestBuilder sourceId(Long sourceId) {
        this.sourceId = sourceId;
        return this;
    }

    public InspectionTestBuilder inventoryId(Long inventoryId) {
        this.inventoryId = inventoryId;
        return this;
    }

    public InspectionTestBuilder productId(Long productId) {
        this.productId = productId;
        return this;
    }

    public InspectionTestBuilder lotId(Long lotId) {
        this.lotId = lotId;
        return this;
    }

    public InspectionTestBuilder sectionId(Long sectionId) {
        this.sectionId = sectionId;
        return this;
    }

    public InspectionTestBuilder warehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
        return this;
    }

    public InspectionTestBuilder inspectionQuantity(int inspectionQuantity) {
        this.inspectionQuantity = inspectionQuantity;
        return this;
    }

    public InspectionTestBuilder inspectorId(Long inspectorId) {
        this.inspectorId = inspectorId;
        return this;
    }

    public InspectionTestBuilder passedQuantity(int passedQuantity) {
        this.passedQuantity = passedQuantity;
        return this;
    }

    public InspectionTestBuilder failedQuantity(int failedQuantity) {
        this.failedQuantity = failedQuantity;
        return this;
    }

    public InspectionTestBuilder defectReason(String defectReason) {
        this.defectReason = defectReason;
        return this;
    }

    /** WAITING 상태 전표 생성 */
    public Inspection buildPending() {
        return Inspection.createPending(sourceType, sourceId, inventoryId, inspectionQuantity,
                productId, lotId, sectionId, warehouseId, LocalDate.of(2026, 12, 31));
    }

    /** IN_PROGRESS 상태 전표 생성 */
    public Inspection buildInProgress() {
        Inspection inspection = buildPending();
        inspection.startInspection(inspectorId);
        return inspection;
    }

    /** COMPLETED 상태 전표 생성 */
    public Inspection buildCompleted() {
        Inspection inspection = buildInProgress();
        inspection.completeInspection(passedQuantity, failedQuantity, defectReason);
        return inspection;
    }
}