package com.kb.cosmetic_wms.inventory.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InvalidInventoryQuantityException extends BusinessException {

    public InvalidInventoryQuantityException(InventoryErrorCode errorCode) {
        super(errorCode);
    }

    public InvalidInventoryQuantityException(InventoryErrorCode errorCode, String customMessage) {
        super(errorCode, customMessage);
    }
}