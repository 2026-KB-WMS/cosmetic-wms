package com.kb.cosmetic_wms.domain.lot.entity;

import com.kb.cosmetic_wms.domain.lot.constants.LotConstants;
import com.kb.cosmetic_wms.domain.lot.enums.LotStatus;
import com.kb.cosmetic_wms.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Lot extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    private String lotNumber;
    private LocalDateTime manufacturingDate;
    private LocalDateTime expirationDate;

    @Enumerated(EnumType.STRING)
    private LotStatus status;

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

    private static void validateProductId(Long productId) {
        if (productId == null) {
            throw new IllegalArgumentException(LotConstants.PRODUCT_REQUIRED_MESSAGE);
        }
    }
}
