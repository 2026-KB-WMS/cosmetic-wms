package com.kb.cosmetic_wms.product.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class DuplicateProductTypeException extends BusinessException {
    public DuplicateProductTypeException() {
        super(ProductErrorCode.DUPLICATE_PRODUCT_TYPE);
    }
}
