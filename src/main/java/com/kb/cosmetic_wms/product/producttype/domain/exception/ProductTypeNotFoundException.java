package com.kb.cosmetic_wms.product.producttype.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class ProductTypeNotFoundException extends BusinessException {
    public ProductTypeNotFoundException() {
        super(ProductTypeErrorCode.PRODUCT_TYPE_NOT_FOUND);
    }
}