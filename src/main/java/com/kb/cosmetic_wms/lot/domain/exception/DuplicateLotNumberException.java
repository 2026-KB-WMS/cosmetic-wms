package com.kb.cosmetic_wms.lot.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class DuplicateLotNumberException extends BusinessException {
    public DuplicateLotNumberException() {
        super(LotErrorCode.DUPLICATE_LOT_NUMBER);
    }
}