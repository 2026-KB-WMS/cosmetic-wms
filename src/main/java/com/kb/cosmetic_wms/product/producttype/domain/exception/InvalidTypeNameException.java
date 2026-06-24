package com.kb.cosmetic_wms.product.producttype.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InvalidTypeNameException extends BusinessException {
    public InvalidTypeNameException() {
        super(ProductTypeErrorCode.INVALID_TYPE_NAME);
    }
}