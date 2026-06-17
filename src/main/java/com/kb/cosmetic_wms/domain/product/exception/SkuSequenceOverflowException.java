package com.kb.cosmetic_wms.domain.product.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class SkuSequenceOverflowException extends BusinessException {
    public SkuSequenceOverflowException() {
        super(ProductErrorCode.SKU_SEQUENCE_OVERFLOW);
    }
}
