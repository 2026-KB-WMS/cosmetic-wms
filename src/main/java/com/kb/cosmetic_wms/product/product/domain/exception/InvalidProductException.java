package com.kb.cosmetic_wms.product.product.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InvalidProductException extends BusinessException {
    public InvalidProductException(String detailMessage) {
        super(ProductErrorCode.INVALID_PRODUCT, detailMessage);
    }
}