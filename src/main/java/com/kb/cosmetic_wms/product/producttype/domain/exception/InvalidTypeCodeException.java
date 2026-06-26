package com.kb.cosmetic_wms.product.producttype.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InvalidTypeCodeException extends BusinessException {
    public InvalidTypeCodeException(String detailMessage) {
        super(ProductTypeErrorCode.INVALID_TYPE_CODE, detailMessage);
    }
}
