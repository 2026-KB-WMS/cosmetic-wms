package com.kb.cosmetic_wms.inventory.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InvalidInventoryTransactionException extends BusinessException {

    public InvalidInventoryTransactionException(InventoryErrorCode errorCode) {
        super(errorCode);
    }

    public InvalidInventoryTransactionException(InventoryErrorCode errorCode, String customMessage) {
        super(errorCode, customMessage);
    }
}