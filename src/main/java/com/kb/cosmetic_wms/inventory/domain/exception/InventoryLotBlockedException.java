package com.kb.cosmetic_wms.inventory.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InventoryLotBlockedException extends BusinessException {
    public InventoryLotBlockedException() {
        super(InventoryErrorCode.INVENTORY_LOT_BLOCKED);
    }
}
