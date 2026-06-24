package com.kb.cosmetic_wms.store.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class StoreValidationException extends BusinessException {
    public StoreValidationException() {
        super(StoreErrorCode.STORE_VALIDATION_FAILED);
    }
}