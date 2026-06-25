package com.kb.cosmetic_wms.lot.domain.model;

import com.kb.cosmetic_wms.lot.domain.constants.LotConstants;
import com.kb.cosmetic_wms.lot.domain.enums.LotStatus;

import java.time.LocalDateTime;

public class Lot {

    private Long id;
    private String lotNumber;
    private LocalDateTime manufacturingDate;
    private LocalDateTime expirationDate;
    private LotStatus status;
    private Long productId;

    private Lot(Long id, String lotNumber, LocalDateTime manufacturingDate,
                LocalDateTime expirationDate, LotStatus status, Long productId) {
        this.id = id;
        this.lotNumber = lotNumber;
        this.manufacturingDate = manufacturingDate;
        this.expirationDate = expirationDate;
        this.status = status;
        this.productId = productId;
    }

    public static Lot create(String lotNumber, LocalDateTime manufacturingDate,
                             LocalDateTime expirationDate, Long productId) {
        validateDates(manufacturingDate, expirationDate);
        validateLotNumber(lotNumber);
        validateProductId(productId);

        return new Lot(null, lotNumber, manufacturingDate, expirationDate,
                LotStatus.AVAILABLE, productId);
    }

    public static Lot reconstitute(Long id, String lotNumber, LocalDateTime manufacturingDate,
                                   LocalDateTime expirationDate, LotStatus status, Long productId) {
        return new Lot(id, lotNumber, manufacturingDate, expirationDate, status, productId);
    }

    public void changeStatus(LotStatus newStatus) {
        this.status = newStatus;
    }

    private static void validateDates(LocalDateTime manufacturingDate, LocalDateTime expirationDate) {
        if (manufacturingDate == null || expirationDate == null) {
            throw new IllegalArgumentException(LotConstants.DATES_REQUIRED_MESSAGE);
        }
        if (manufacturingDate.isAfter(expirationDate)) {
            throw new IllegalArgumentException(LotConstants.INVALID_MANUFACTURE_DATE_MESSAGE);
        }
    }

    private static void validateLotNumber(String lotNumber) {
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

    public Long getId() { return id; }
    public String getLotNumber() { return lotNumber; }
    public LocalDateTime getManufacturingDate() { return manufacturingDate; }
    public LocalDateTime getExpirationDate() { return expirationDate; }
    public LotStatus getStatus() { return status; }
    public Long getProductId() { return productId; }
}