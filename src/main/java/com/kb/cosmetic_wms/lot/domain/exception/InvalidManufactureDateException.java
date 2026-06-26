package com.kb.cosmetic_wms.lot.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InvalidManufactureDateException extends BusinessException {
    public InvalidManufactureDateException() {
        super(LotErrorCode.INVALID_MANUFACTURE_DATE);
    }
}