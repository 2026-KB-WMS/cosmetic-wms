package com.kb.cosmetic_wms.inventory.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InventoryStateTransitionException extends BusinessException {

    public InventoryStateTransitionException(InventoryErrorCode errorCode) {
        super(errorCode);
    }
}