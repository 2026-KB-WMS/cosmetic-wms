package com.kb.cosmetic_wms.product.product.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InvalidPriceException extends BusinessException {
    public InvalidPriceException() {
        super(ProductErrorCode.INVALID_PRICE);
    }
}