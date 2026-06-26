package com.kb.cosmetic_wms.store.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class DuplicateStoreException extends BusinessException {
    public DuplicateStoreException() {
        super(StoreErrorCode.DUPLICATE_STORE);
    }
}