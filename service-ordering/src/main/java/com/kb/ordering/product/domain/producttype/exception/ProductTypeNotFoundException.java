package com.kb.ordering.product.domain.producttype.exception;

import com.kb.common.error.BusinessException;

public class ProductTypeNotFoundException extends BusinessException {
    public ProductTypeNotFoundException() {
        super(ProductTypeErrorCode.PRODUCT_TYPE_NOT_FOUND);
    }
}