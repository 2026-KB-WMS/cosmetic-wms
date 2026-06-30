package com.kb.cosmetic_wms.putaway.adapter.out.persistence;

import com.kb.cosmetic_wms.global.common.BaseEntity;
import com.kb.cosmetic_wms.putaway.domain.enums.PutawayStatus;
import com.kb.cosmetic_wms.putaway.domain.model.PutawayOrder;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "putaway_order")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class PutawayOrderEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "putaway_order_id")
    private Long id;

    @Column(name = "inspection_id", nullable = false)
    private Long inspectionId;

    @Column(name = "lot_id", nullable = false)
    private Long lotId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(name = "source_section_id", nullable = false)
    private Long sourceSectionId;

    @Column(name = "target_section_id", nullable = false)
    private Long targetSectionId;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PutawayStatus status;

    private PutawayOrderEntity(Long id, Long inspectionId, Long lotId, Long productId,
                               Long warehouseId, Long sourceSectionId, Long targetSectionId,
                               int quantity, PutawayStatus status) {
        this.id = id;
        this.inspectionId = inspectionId;
        this.lotId = lotId;
        this.productId = productId;
        this.warehouseId = warehouseId;
        this.sourceSectionId = sourceSectionId;
        this.targetSectionId = targetSectionId;
        this.quantity = quantity;
        this.status = status;
    }

    static PutawayOrderEntity fromDomain(PutawayOrder domain) {
        return new PutawayOrderEntity(
                domain.getId(),
                domain.getInspectionId(),
                domain.getLotId(),
                domain.getProductId(),
                domain.getWarehouseId(),
                domain.getSourceSectionId(),
                domain.getTargetSectionId(),
                domain.getQuantity(),
                domain.getStatus()
        );
    }

    PutawayOrder toDomain() {
        return PutawayOrder.reconstitute(id, inspectionId, lotId, productId,
                warehouseId, sourceSectionId, targetSectionId, quantity, status);
    }
}
