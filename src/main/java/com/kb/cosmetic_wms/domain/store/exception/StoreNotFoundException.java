package com.kb.cosmetic_wms.domain.store.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class StoreNotFoundException extends BusinessException {
    public StoreNotFoundException() {
        super(StoreErrorCode.STORE_NOT_FOUND);
    }
}
