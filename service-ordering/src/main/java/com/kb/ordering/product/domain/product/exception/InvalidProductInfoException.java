package com.kb.ordering.product.domain.product.exception;

import com.kb.common.error.BusinessException;

public class InvalidProductInfoException extends BusinessException {
    public InvalidProductInfoException(String detailMessage) {
        super(ProductErrorCode.INVALID_PRODUCT_INFO, detailMessage);
    }
}