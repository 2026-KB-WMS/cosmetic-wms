package com.kb.cosmetic_wms.inventory.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InventoryMergeException extends BusinessException {

    public InventoryMergeException(InventoryErrorCode errorCode) {
        super(errorCode);
    }
}
