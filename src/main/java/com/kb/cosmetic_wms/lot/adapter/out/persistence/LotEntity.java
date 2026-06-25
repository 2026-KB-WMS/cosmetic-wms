package com.kb.cosmetic_wms.lot.adapter.out.persistence;

import com.kb.cosmetic_wms.global.common.BaseEntity;
import com.kb.cosmetic_wms.lot.domain.enums.LotStatus;
import com.kb.cosmetic_wms.lot.domain.model.Lot;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Check;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "lot",
        uniqueConstraints = @UniqueConstraint(name = "uq_lot_number", columnNames = "lot_number")
)
@Check(name = "chk_lot_date", constraints = "manufacturing_date <= expiration_date")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class LotEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lot_id")
    private Long id;

    @Column(name = "lot_number", nullable = false, length = 50)
    private String lotNumber;

    @Column(name = "manufacturing_date", nullable = false)
    private LocalDateTime manufacturingDate;

    @Column(name = "expiration_date", nullable = false)
    private LocalDateTime expirationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LotStatus status;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    private LotEntity(Long id, String lotNumber, LocalDateTime manufacturingDate,
                      LocalDateTime expirationDate, LotStatus status, Long productId) {
        this.id = id;
        this.lotNumber = lotNumber;
        this.manufacturingDate = manufacturingDate;
        this.expirationDate = expirationDate;
        this.status = status;
        this.productId = productId;
    }

    static LotEntity fromDomain(Lot lot) {
        return new LotEntity(
                lot.getId(),
                lot.getLotNumber(),
                lot.getManufacturingDate(),
                lot.getExpirationDate(),
                lot.getStatus(),
                lot.getProductId()
        );
    }

    Lot toDomain() {
        return Lot.reconstitute(id, lotNumber, manufacturingDate, expirationDate, status, productId);
    }
}