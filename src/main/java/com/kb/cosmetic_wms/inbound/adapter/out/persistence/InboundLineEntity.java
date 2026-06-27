package com.kb.cosmetic_wms.inbound.adapter.out.persistence;

import com.kb.cosmetic_wms.global.common.BaseEntity;
import com.kb.cosmetic_wms.inbound.domain.model.InboundLine;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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

    @Column(name = "manufacture_date", nullable = false)
    private LocalDate manufactureDate;

    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inbound_id", nullable = false)
    private InboundEntity inbound;

    private InboundLineEntity(Long id, Long productId, int orderedQuantity, int receivedQuantity,
                               LocalDate manufactureDate, LocalDate expirationDate) {
        this.id = id;
        this.productId = productId;
        this.orderedQuantity = orderedQuantity;
        this.receivedQuantity = receivedQuantity;
        this.manufactureDate = manufactureDate;
        this.expirationDate = expirationDate;
    }

    static InboundLineEntity fromDomain(InboundLine line) {
        return new InboundLineEntity(
                line.getId(), line.getProductId(),
                line.getOrderedQuantity(), line.getReceivedQuantity(),
                line.getManufactureDate(), line.getExpirationDate()
        );
    }

    void setInbound(InboundEntity inbound) {
        this.inbound = inbound;
    }

    InboundLine toDomain() {
        return InboundLine.reconstitute(
                id, productId, orderedQuantity, receivedQuantity,
                manufactureDate, expirationDate
        );
    }
}