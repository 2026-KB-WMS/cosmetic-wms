package com.kb.cosmetic_wms.inbound.adapter.out.persistence;

import com.kb.cosmetic_wms.global.common.BaseEntity;
import com.kb.cosmetic_wms.inbound.domain.enums.InspectionStatus;
import com.kb.cosmetic_wms.inbound.domain.model.InboundItem;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "inbound_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class InboundItemEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inbound_item_id")
    private Long id;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "manufacture_date", nullable = false)
    private LocalDate manufactureDate;

    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "inspection_status", nullable = false, length = 20)
    private InspectionStatus inspectionStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inbound_id", nullable = false)
    private InboundEntity inbound;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "lot_id")
    private Long lotId;

    @Column(name = "section_id")
    private Long sectionId;

    private InboundItemEntity(Long id, int quantity, LocalDate manufactureDate, LocalDate expirationDate,
                              InspectionStatus inspectionStatus, Long productId, Long lotId, Long sectionId) {
        this.id = id;
        this.quantity = quantity;
        this.manufactureDate = manufactureDate;
        this.expirationDate = expirationDate;
        this.inspectionStatus = inspectionStatus;
        this.productId = productId;
        this.lotId = lotId;
        this.sectionId = sectionId;
    }

    static InboundItemEntity fromDomain(InboundItem item) {
        return new InboundItemEntity(
                item.getId(), item.getQuantity(),
                item.getManufactureDate(), item.getExpirationDate(),
                item.getInspectionStatus(), item.getProductId(),
                item.getLotId(), item.getSectionId()
        );
    }

    void setInbound(InboundEntity inbound) {
        this.inbound = inbound;
    }

    InboundItem toDomain() {
        return InboundItem.reconstitute(
                id, productId, quantity, manufactureDate, expirationDate,
                inspectionStatus, lotId, sectionId
        );
    }
}
