package com.kb.cosmetic_wms.product.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class ProductNotFoundException extends BusinessException {
    public ProductNotFoundException() {
        super(ProductErrorCode.PRODUCT_NOT_FOUND);
    }
}