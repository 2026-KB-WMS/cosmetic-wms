package com.kb.cosmetic_wms.domain.storage.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class StorageValidationException extends BusinessException {
    public StorageValidationException() {
        super(StorageErrorCode.INVALID_STORAGE_STATE);
    }
}
