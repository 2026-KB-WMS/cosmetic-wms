package com.kb.cosmetic_wms.domain.inventory.entity;

import com.kb.cosmetic_wms.domain.inventory.constants.LotConstants;
import com.kb.cosmetic_wms.domain.inventory.enums.LotStatus;
import com.kb.cosmetic_wms.domain.product.entity.Product;
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

    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    private Lot(
            String lotNumber, LocalDateTime manufacturingDate,
            LocalDateTime expirationDate, Product product
    ) {
        this.lotNumber = lotNumber;
        this.manufacturingDate = manufacturingDate;
        this.expirationDate = expirationDate;
        this.status = LotStatus.AVAILABLE;
        this.product = product;
    }

    public static Lot create(
            String lotNumber, LocalDateTime manufacturingDate,
            LocalDateTime expirationDate, Product product
    ) {
        validateDates(manufacturingDate, expirationDate);
        validateLotNo(lotNumber);

        return new Lot(lotNumber, manufacturingDate, expirationDate, product);
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
}
