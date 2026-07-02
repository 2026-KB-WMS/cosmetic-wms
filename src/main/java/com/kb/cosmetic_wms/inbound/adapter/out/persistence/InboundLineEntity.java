package com.kb.cosmetic_wms.inbound.adapter.out.persistence;

import com.kb.cosmetic_wms.global.common.BaseEntity;
import com.kb.cosmetic_wms.inbound.domain.model.InboundLine;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "inbound_line")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class InboundLineEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inbound_line_id")
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "ordered_quantity", nullable = false)
    private int orderedQuantity;

    @Column(name = "received_quantity", nullable = false)
    private int receivedQuantity;

    @Column(name = "manufacturer_lot_no")
    private String manufacturerLotNumber;

    @Column(name = "manufacturing_date")
    private LocalDateTime manufacturingDate;

    @Column(name = "expiration_date")
    private LocalDateTime expirationDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inbound_id", nullable = false)
    private InboundEntity inbound;

    private InboundLineEntity(Long id, Long productId, int orderedQuantity, int receivedQuantity,
                               String manufacturerLotNumber, LocalDateTime manufacturingDate,
                               LocalDateTime expirationDate) {
        this.id = id;
        this.productId = productId;
        this.orderedQuantity = orderedQuantity;
        this.receivedQuantity = receivedQuantity;
        this.manufacturerLotNumber = manufacturerLotNumber;
        this.manufacturingDate = manufacturingDate;
        this.expirationDate = expirationDate;
    }

    static InboundLineEntity fromDomain(InboundLine line) {
        return new InboundLineEntity(
                line.getId(), line.getProductId(),
                line.getOrderedQuantity(), line.getReceivedQuantity(),
                line.getManufacturerLotNumber(),
                line.getManufacturingDate(), line.getExpirationDate()
        );
    }

    Long getId() {
        return id;
    }

    void setInbound(InboundEntity inbound) {
        this.inbound = inbound;
    }

    void updateFrom(InboundLine domain) {
        this.receivedQuantity = domain.getReceivedQuantity();
        this.manufacturerLotNumber = domain.getManufacturerLotNumber();
        this.manufacturingDate = domain.getManufacturingDate();
        this.expirationDate = domain.getExpirationDate();
    }

    InboundLine toDomain() {
        return InboundLine.reconstitute(
                id, productId, orderedQuantity, receivedQuantity,
                manufacturerLotNumber, manufacturingDate, expirationDate
        );
    }
}
