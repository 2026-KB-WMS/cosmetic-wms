package com.kb.ordering.product.domain.product.exception;

import com.kb.common.error.BusinessException;

public class InvalidBrandNameException extends BusinessException {
    public InvalidBrandNameException() {
        super(ProductErrorCode.INVALID_BRAND_NAME);
    }
}