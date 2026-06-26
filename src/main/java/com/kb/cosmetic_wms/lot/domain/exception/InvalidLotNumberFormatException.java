package com.kb.cosmetic_wms.lot.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InvalidLotNumberFormatException extends BusinessException {
    public InvalidLotNumberFormatException() {
        super(LotErrorCode.INVALID_LOT_NUMBER_FORMAT);
    }
}