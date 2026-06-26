package com.kb.cosmetic_wms.lot.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class LotNumberRequiredException extends BusinessException {
    public LotNumberRequiredException() {
        super(LotErrorCode.LOT_NUMBER_REQUIRED);
    }
}