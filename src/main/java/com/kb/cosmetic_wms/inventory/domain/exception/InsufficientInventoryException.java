package com.kb.cosmetic_wms.inventory.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InsufficientInventoryException extends BusinessException {

    public InsufficientInventoryException() {
        super(InventoryErrorCode.INSUFFICIENT_STOCK);
    }
}