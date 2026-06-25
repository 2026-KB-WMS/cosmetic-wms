package com.kb.cosmetic_wms.lot.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class LotDatesRequiredException extends BusinessException {
    public LotDatesRequiredException() {
        super(LotErrorCode.LOT_DATES_REQUIRED);
    }
}