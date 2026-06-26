package com.kb.cosmetic_wms.storage.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class StorageValidationException extends BusinessException {

    public StorageValidationException(StorageErrorCode errorCode) {
        super(errorCode);
    }
}