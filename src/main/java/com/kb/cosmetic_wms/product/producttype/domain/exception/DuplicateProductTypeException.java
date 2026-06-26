package com.kb.cosmetic_wms.product.producttype.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class DuplicateProductTypeException extends BusinessException {
    public DuplicateProductTypeException() {
        super(ProductTypeErrorCode.DUPLICATE_PRODUCT_TYPE);
    }
}