package com.kb.ordering.product.domain.product.exception;

import com.kb.common.error.BusinessException;

public class InvalidProductNameException extends BusinessException {
    public InvalidProductNameException() {
        super(ProductErrorCode.INVALID_PRODUCT_NAME);
    }
}