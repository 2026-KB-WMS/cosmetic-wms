package com.kb.cosmetic_wms.product.product.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class DuplicateProductException extends BusinessException {
    public DuplicateProductException() {
        super(ProductErrorCode.DUPLICATE_PRODUCT);
    }
}