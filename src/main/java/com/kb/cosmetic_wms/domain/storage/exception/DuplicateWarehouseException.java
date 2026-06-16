package com.kb.cosmetic_wms.domain.storage.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class DuplicateWarehouseException extends BusinessException {
    public DuplicateWarehouseException() {
        super(StorageErrorCode.DUPLICATE_WAREHOUSE);
    }
}
