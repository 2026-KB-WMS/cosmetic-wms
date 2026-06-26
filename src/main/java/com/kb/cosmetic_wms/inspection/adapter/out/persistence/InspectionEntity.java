package com.kb.cosmetic_wms.inspection.adapter.out.persistence;

import com.kb.cosmetic_wms.global.common.BaseEntity;
import com.kb.cosmetic_wms.inspection.domain.enums.InspectionSourceType;
import com.kb.cosmetic_wms.inspection.domain.enums.InspectionStatus;
import com.kb.cosmetic_wms.inspection.domain.model.Inspection;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "quality_inspection")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class InspectionEntity extends BaseEntity {

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

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    private InspectionEntity(Long id, InspectionSourceType sourceType, Long sourceId,
                             Long inventoryId, Long productId, Long lotId,
                             Long sectionId, Long warehouseId, Long inspectorId,
                             InspectionStatus status, int inspectionQuantity,
                             int passedQuantity, int failedQuantity, String defectReason,
                             LocalDate expiryDate) {
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

    static InspectionEntity fromDomain(Inspection inspection) {
        return new InspectionEntity(
                inspection.getId(),
                inspection.getSourceType(),
                inspection.getSourceId(),
                inspection.getInventoryId(),
                inspection.getProductId(),
                inspection.getLotId(),
                inspection.getSectionId(),
                inspection.getWarehouseId(),
                inspection.getInspectorId(),
                inspection.getStatus(),
                inspection.getInspectionQuantity(),
                inspection.getPassedQuantity(),
                inspection.getFailedQuantity(),
                inspection.getDefectReason(),
                inspection.getExpiryDate()
        );
    }

    Inspection toDomain() {
        return Inspection.reconstitute(
                id, sourceType, sourceId, inventoryId, productId, lotId,
                sectionId, warehouseId, inspectorId, status, inspectionQuantity,
                passedQuantity, failedQuantity, defectReason, expiryDate
        );
    }
}