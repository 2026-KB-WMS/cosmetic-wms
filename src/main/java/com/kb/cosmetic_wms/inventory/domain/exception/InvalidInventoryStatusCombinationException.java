package com.kb.cosmetic_wms.inventory.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InvalidInventoryStatusCombinationException extends BusinessException {

    public InvalidInventoryStatusCombinationException(InventoryErrorCode errorCode) {
        super(errorCode);
    }

    public InvalidInventoryStatusCombinationException(InventoryErrorCode errorCode, String customMessage) {
        super(errorCode, customMessage);
    }
}