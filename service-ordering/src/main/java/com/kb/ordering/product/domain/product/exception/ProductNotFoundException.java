package com.kb.ordering.product.domain.product.exception;

import com.kb.common.error.BusinessException;

public class ProductNotFoundException extends BusinessException {
    public ProductNotFoundException() {
        super(ProductErrorCode.PRODUCT_NOT_FOUND);
    }
}