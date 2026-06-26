package com.kb.cosmetic_wms.inspection.domain.model;

import com.kb.cosmetic_wms.inspection.domain.enums.InspectionSourceType;
import com.kb.cosmetic_wms.inspection.domain.enums.InspectionStatus;
import com.kb.cosmetic_wms.inspection.domain.exception.InspectionCompleteNotAllowedException;
import com.kb.cosmetic_wms.inspection.domain.exception.InspectionDefectReasonRequiredException;
import com.kb.cosmetic_wms.inspection.domain.exception.InspectionInspectorIdRequiredException;
import com.kb.cosmetic_wms.inspection.domain.exception.InspectionNegativeQuantityException;
import com.kb.cosmetic_wms.inspection.domain.exception.InspectionQuantityInvalidException;
import com.kb.cosmetic_wms.inspection.domain.exception.InspectionQuantityMismatchException;
import com.kb.cosmetic_wms.inspection.domain.exception.InspectionSourceIdRequiredException;
import com.kb.cosmetic_wms.inspection.domain.exception.InspectionSourceTypeRequiredException;
import com.kb.cosmetic_wms.inspection.domain.exception.InspectionStartNotAllowedException;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class Inspection {

    private Long id;
    private InspectionSourceType sourceType;
    private Long sourceId;
    private Long inventoryId;
    private Long productId;
    private Long lotId;
    private Long sectionId;
    private Long warehouseId;
    private Long inspectorId;
    private InspectionStatus status;
    private int inspectionQuantity;
    private int passedQuantity;
    private int failedQuantity;
    private String defectReason;
    private LocalDate expiryDate;

    private Inspection(Long id, InspectionSourceType sourceType, Long sourceId, Long inventoryId,
                       Long productId, Long lotId, Long sectionId, Long warehouseId,
                       Long inspectorId, InspectionStatus status, int inspectionQuantity,
                       int passedQuantity, int failedQuantity, String defectReason, LocalDate expiryDate) {
        this.id = id;
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
        this.expiryDate = expiryDate;
    }

    public static Inspection createPending(InspectionSourceType sourceType, Long sourceId,
                                           Long inventoryId, int inspectionQuantity,
                                           Long productId, Long lotId, Long sectionId, Long warehouseId,
                                           LocalDate expiryDate) {
        validateInitial(sourceType, sourceId, inspectionQuantity);
        return new Inspection(null, sourceType, sourceId, inventoryId,
                productId, lotId, sectionId, warehouseId,
                null, InspectionStatus.WAITING, inspectionQuantity, 0, 0, null, expiryDate);
    }

    public static Inspection reconstitute(Long id, InspectionSourceType sourceType, Long sourceId,
                                          Long inventoryId, Long productId, Long lotId,
                                          Long sectionId, Long warehouseId, Long inspectorId,
                                          InspectionStatus status, int inspectionQuantity,
                                          int passedQuantity, int failedQuantity, String defectReason,
                                          LocalDate expiryDate) {
        return new Inspection(id, sourceType, sourceId, inventoryId,
                productId, lotId, sectionId, warehouseId, inspectorId,
                status, inspectionQuantity, passedQuantity, failedQuantity, defectReason, expiryDate);
    }

    public void startInspection(Long inspectorId) {
        if (!status.canTransitionTo(InspectionStatus.IN_PROGRESS)) {
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
        if (!status.canTransitionTo(InspectionStatus.COMPLETED)) {
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