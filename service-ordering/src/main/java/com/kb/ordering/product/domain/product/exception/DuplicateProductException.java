package com.kb.ordering.product.domain.product.exception;

import com.kb.common.error.BusinessException;

public class DuplicateProductException extends BusinessException {
    public DuplicateProductException() {
        super(ProductErrorCode.DUPLICATE_PRODUCT);
    }
}