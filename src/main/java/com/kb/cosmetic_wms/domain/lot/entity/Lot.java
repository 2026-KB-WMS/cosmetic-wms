package com.kb.cosmetic_wms.domain.lot.entity;

import com.kb.cosmetic_wms.domain.lot.constants.LotConstants;
import com.kb.cosmetic_wms.domain.lot.enums.LotStatus;
import com.kb.cosmetic_wms.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
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
@Getter
public class Lot extends BaseEntity {

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

    private Lot(
            String lotNumber, LocalDateTime manufacturingDate,
            LocalDateTime expirationDate, Long productId
    ) {
        this.lotNumber = lotNumber;
        this.manufacturingDate = manufacturingDate;
        this.expirationDate = expirationDate;
        this.status = LotStatus.AVAILABLE;
        this.productId = productId;
    }

    public static Lot create(
            String lotNumber, LocalDateTime manufacturingDate,
            LocalDateTime expirationDate, Long productId
    ) {
        validateDates(manufacturingDate, expirationDate);
        validateLotNo(lotNumber);
        validateProductId(productId);

        return new Lot(lotNumber, manufacturingDate, expirationDate, productId);
    }

    private static void validateDates(LocalDateTime manufacturingDate, LocalDateTime expirationDate) {
        if (manufacturingDate == null || expirationDate == null) {
            throw new IllegalArgumentException(LotConstants.DATES_REQUIRED_MESSAGE);
        }

        if (manufacturingDate.isAfter(expirationDate)) {
            throw new IllegalArgumentException(LotConstants.INVALID_MANUFACTURE_DATE_MESSAGE);
        }
    }

    private static void validateLotNo(String lotNumber) {
        if (lotNumber == null || lotNumber.isBlank()) {
            throw new IllegalArgumentException(LotConstants.LOT_NO_REQUIRED_MESSAGE);
        }

        if (!LotConstants.LOT_NO_PATTERN.matcher(lotNumber).matches()) {
            throw new IllegalArgumentException(LotConstants.INVALID_LOT_NO_FORMAT_MESSAGE);
        }
    }

    public void changeStatus(LotStatus newStatus) {
        this.status = newStatus;
    }

    private static void validateProductId(Long productId) {
        if (productId == null) {
            throw new IllegalArgumentException(LotConstants.PRODUCT_REQUIRED_MESSAGE);
        }
    }
}
