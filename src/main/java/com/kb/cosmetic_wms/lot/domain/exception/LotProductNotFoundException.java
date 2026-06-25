package com.kb.cosmetic_wms.lot.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class LotProductNotFoundException extends BusinessException {
    public LotProductNotFoundException() {
        super(LotErrorCode.PRODUCT_NOT_FOUND);
    }
}