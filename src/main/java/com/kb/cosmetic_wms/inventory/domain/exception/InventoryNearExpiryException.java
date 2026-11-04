package com.kb.cosmetic_wms.inventory.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InventoryNearExpiryException extends BusinessException {
    public InventoryNearExpiryException() {
        super(InventoryErrorCode.INVENTORY_NEAR_EXPIRY);
    }
}
