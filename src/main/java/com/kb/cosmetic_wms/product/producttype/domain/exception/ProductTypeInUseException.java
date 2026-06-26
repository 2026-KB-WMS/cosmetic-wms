package com.kb.cosmetic_wms.product.producttype.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class ProductTypeInUseException extends BusinessException {
    public ProductTypeInUseException() {
        super(ProductTypeErrorCode.PRODUCT_TYPE_IN_USE);
    }
}