package com.kb.cosmetic_wms.storage.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InvalidTargetTempException extends BusinessException {

    public InvalidTargetTempException() {
        super(StorageErrorCode.INVALID_TARGET_TEMP);
    }
}