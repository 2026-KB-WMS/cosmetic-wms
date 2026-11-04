package com.kb.cosmetic_wms.inventory.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InvalidShelfLifePolicyException extends BusinessException {
    public InvalidShelfLifePolicyException() {
        super(InventoryErrorCode.INVALID_SHELF_LIFE_POLICY);
    }
}
