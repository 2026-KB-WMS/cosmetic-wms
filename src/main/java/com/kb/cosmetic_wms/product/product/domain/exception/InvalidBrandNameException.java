package com.kb.cosmetic_wms.product.product.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InvalidBrandNameException extends BusinessException {
    public InvalidBrandNameException() {
        super(ProductErrorCode.INVALID_BRAND_NAME);
    }
}