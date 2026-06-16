package com.kb.cosmetic_wms.domain.storage.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class WarehouseNotFoundException extends BusinessException {
    public WarehouseNotFoundException() {
        super(StorageErrorCode.STORAGE_NOT_FOUND);
    }
}
