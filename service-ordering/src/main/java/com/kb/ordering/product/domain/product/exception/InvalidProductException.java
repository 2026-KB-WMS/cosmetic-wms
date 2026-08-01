package com.kb.ordering.product.domain.product.exception;

import com.kb.common.error.BusinessException;

public class InvalidProductException extends BusinessException {
    public InvalidProductException(String detailMessage) {
        super(ProductErrorCode.INVALID_PRODUCT, detailMessage);
    }
}