package com.kb.ordering.product.domain.product.exception;

import com.kb.common.error.BusinessException;

public class InvalidPriceException extends BusinessException {
    public InvalidPriceException() {
        super(ProductErrorCode.INVALID_PRICE);
    }
}