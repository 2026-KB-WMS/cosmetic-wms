package com.kb.ordering.product.domain.producttype.exception;

import com.kb.common.error.BusinessException;

public class InvalidTypeNameException extends BusinessException {
    public InvalidTypeNameException() {
        super(ProductTypeErrorCode.INVALID_TYPE_NAME);
    }
}