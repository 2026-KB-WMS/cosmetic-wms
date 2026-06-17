package com.kb.cosmetic_wms.domain.product.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class DuplicateProductException extends BusinessException {
    public DuplicateProductException() {
        super(ProductErrorCode.DUPLICATE_PRODUCT);
    }
}
