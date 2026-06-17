package com.kb.cosmetic_wms.domain.product.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class ProductTypeNotFoundException extends BusinessException {
    public ProductTypeNotFoundException() {
        super(ProductErrorCode.PRODUCT_TYPE_NOT_FOUND);
    }
}
