package com.kb.cosmetic_wms.lot.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class LotProductIdRequiredException extends BusinessException {
    public LotProductIdRequiredException() {
        super(LotErrorCode.LOT_PRODUCT_ID_REQUIRED);
    }
}