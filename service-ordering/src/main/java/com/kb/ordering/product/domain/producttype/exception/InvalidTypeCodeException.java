package com.kb.ordering.product.domain.producttype.exception;

import com.kb.common.error.BusinessException;

public class InvalidTypeCodeException extends BusinessException {
    public InvalidTypeCodeException(String detailMessage) {
        super(ProductTypeErrorCode.INVALID_TYPE_CODE, detailMessage);
    }
}
