package com.kb.cosmetic_wms.domain.inventory.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InventoryNotFoundException extends BusinessException {
    public InventoryNotFoundException() {
        super(InventoryErrorCode.INVENTORY_NOT_FOUND);
    }
}
