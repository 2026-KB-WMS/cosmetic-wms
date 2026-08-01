package com.kb.ordering.product.domain.producttype.exception;

import com.kb.common.error.BusinessException;

public class DuplicateProductTypeException extends BusinessException {
    public DuplicateProductTypeException() {
        super(ProductTypeErrorCode.DUPLICATE_PRODUCT_TYPE);
    }
}