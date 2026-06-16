package com.kb.cosmetic_wms.domain.storage.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class StorageExceedCapacityException extends BusinessException {
    public StorageExceedCapacityException() {
        super(StorageErrorCode.EXCEED_WAREHOUSE_CAPACITY);
    }
}
