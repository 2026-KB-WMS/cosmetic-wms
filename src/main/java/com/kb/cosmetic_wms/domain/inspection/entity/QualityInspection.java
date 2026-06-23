package com.kb.cosmetic_wms.domain.inspection.entity;

import com.kb.cosmetic_wms.domain.inspection.enums.InspectionSourceType;
import com.kb.cosmetic_wms.domain.inspection.enums.InspectionStatus;
import com.kb.cosmetic_wms.domain.inspection.exception.InspectionCompleteNotAllowedException;
import com.kb.cosmetic_wms.domain.inspection.exception.InspectionDefectReasonRequiredException;
import com.kb.cosmetic_wms.domain.inspection.exception.InspectionInspectorIdRequiredException;
import com.kb.cosmetic_wms.domain.inspection.exception.InspectionNegativeQuantityException;
import com.kb.cosmetic_wms.domain.inspection.exception.InspectionQuantityInvalidException;
import com.kb.cosmetic_wms.domain.inspection.exception.InspectionQuantityMismatchException;
import com.kb.cosmetic_wms.domain.inspection.exception.InspectionSourceIdRequiredException;
import com.kb.cosmetic_wms.domain.inspection.exception.InspectionSourceTypeRequiredException;
import com.kb.cosmetic_wms.domain.inspection.exception.InspectionStartNotAllowedException;
import com.kb.cosmetic_wms.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "quality_inspection")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class QualityInspection extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inspection_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 20)
    private InspectionSourceType sourceType;

    @Column(name = "source_id", nullable = false)
    private Long sourceId;

    @Column(name = "inventory_id")
    private Long inventoryId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "lot_id", nullable = false)
    private Long lotId;

    @Column(name = "section_id", nullable = false)
    private Long sectionId;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(name = "inspector_id")
    private Long inspectorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InspectionStatus status;

    @Column(name = "inspection_quantity", nullable = false)
    private int inspectionQuantity;

    @Column(name = "passed_quantity", nullable = false)
    private int passedQuantity;

    @Column(name = "failed_quantity", nullable = false)
    private int failedQuantity;

    @Column(name = "defect_reason", length = 100)
    private String defectReason;

    @Builder(access = AccessLevel.PRIVATE)
    private QualityInspection(InspectionSourceType sourceType, Long sourceId, Long inventoryId,
                              Long productId, Long lotId, Long sectionId, Long warehouseId,
                              Long inspectorId, InspectionStatus status, int inspectionQuantity,
                              int passedQuantity, int failedQuantity, String defectReason) {
        this.sourceType = sourceType;
        this.sourceId = sourceId;
        this.inventoryId = inventoryId;
        this.productId = productId;
        this.lotId = lotId;
        this.sectionId = sectionId;
        this.warehouseId = warehouseId;
        this.inspectorId = inspectorId;
        this.status = status;
        this.inspectionQuantity = inspectionQuantity;
        this.passedQuantity = passedQuantity;
        this.failedQuantity = failedQuantity;
        this.defectReason = defectReason;
    }

    public static QualityInspection createPending(InspectionSourceType sourceType, Long sourceId,
                                                  Long inventoryId, int inspectionQuantity,
                                                  Long productId, Long lotId, Long sectionId, Long warehouseId) {
        validateInitial(sourceType, sourceId, inspectionQuantity);

        return QualityInspection.builder()
                .sourceType(sourceType)
                .sourceId(sourceId)
                .inventoryId(inventoryId)
                .productId(productId)
                .lotId(lotId)
                .sectionId(sectionId)
                .warehouseId(warehouseId)
                .status(InspectionStatus.WAITING)
                .inspectionQuantity(inspectionQuantity)
                .passedQuantity(0)
                .failedQuantity(0)
                .build();
    }

    public void startInspection(Long inspectorId) {
        if (this.status != InspectionStatus.WAITING) {
            throw new InspectionStartNotAllowedException();
        }
        if (inspectorId == null) {
            throw new InspectionInspectorIdRequiredException();
        }

        this.inspectorId = inspectorId;
        this.status = InspectionStatus.IN_PROGRESS;
    }

    public void completeInspection(int passedQty, int failedQty, String defectReason) {
        validateCompletion(passedQty, failedQty, defectReason);

        this.passedQuantity = passedQty;
        this.failedQuantity = failedQty;
        this.defectReason = (failedQty > 0) ? defectReason.trim() : null;
        this.status = InspectionStatus.COMPLETED;
    }

    private static void validateInitial(InspectionSourceType sourceType, Long sourceId, int inspectionQuantity) {
        if (sourceType == null) {
            throw new InspectionSourceTypeRequiredException();
        }
        if (sourceId == null) {
            throw new InspectionSourceIdRequiredException();
        }
        if (inspectionQuantity <= 0) {
            throw new InspectionQuantityInvalidException();
        }
    }

    private void validateCompletion(int passedQty, int failedQty, String defectReason) {
        if (this.status != InspectionStatus.IN_PROGRESS) {
            throw new InspectionCompleteNotAllowedException();
        }
        if (passedQty < 0 || failedQty < 0) {
            throw new InspectionNegativeQuantityException();
        }
        if (passedQty + failedQty != this.inspectionQuantity) {
            throw new InspectionQuantityMismatchException(this.inspectionQuantity);
        }
        if (failedQty > 0 && (defectReason == null || defectReason.isBlank())) {
            throw new InspectionDefectReasonRequiredException();
        }
    }
}
