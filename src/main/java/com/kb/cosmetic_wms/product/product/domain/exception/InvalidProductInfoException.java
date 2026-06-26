package com.kb.cosmetic_wms.product.product.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InvalidProductInfoException extends BusinessException {
    public InvalidProductInfoException(String detailMessage) {
        super(ProductErrorCode.INVALID_PRODUCT_INFO, detailMessage);
    }
}