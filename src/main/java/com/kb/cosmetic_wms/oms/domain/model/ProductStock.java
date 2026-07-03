package com.kb.cosmetic_wms.oms.domain.model;

import com.kb.cosmetic_wms.oms.domain.exception.OmsErrorCode;
import com.kb.cosmetic_wms.oms.domain.exception.OmsValidationException;

import java.time.LocalDate;

public record ProductStock(int availableQuantity, LocalDate earliestExpiryDate) {

    public ProductStock {
        if (availableQuantity < 0) {
            throw new OmsValidationException(OmsErrorCode.INVALID_PRODUCT_STOCK);
        }
        if (availableQuantity > 0 && earliestExpiryDate == null) {
            throw new OmsValidationException(OmsErrorCode.INVALID_PRODUCT_STOCK);
        }
    }

    public static ProductStock empty() {
        return new ProductStock(0, null);
    }
}
