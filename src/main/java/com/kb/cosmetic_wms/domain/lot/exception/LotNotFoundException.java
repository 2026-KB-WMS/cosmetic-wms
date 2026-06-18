package com.kb.cosmetic_wms.domain.lot.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class LotNotFoundException extends BusinessException {
    public LotNotFoundException() {
        super(LotErrorCode.LOT_NOT_FOUND);
    }
}
