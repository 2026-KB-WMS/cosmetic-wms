package com.kb.cosmetic_wms.product.product.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InvalidProductNameException extends BusinessException {
    public InvalidProductNameException() {
        super(ProductErrorCode.INVALID_PRODUCT_NAME);
    }
}