package com.kb.cosmetic_wms.inventory.domain.model;

import com.kb.cosmetic_wms.inventory.domain.exception.InventoryErrorCode;
import com.kb.cosmetic_wms.inventory.domain.exception.InvalidInventoryQuantityException;

public record InventoryQuantity(int total, int available) {

    public static InventoryQuantity of(int total, int available) {
        if (total < 0) {
            throw new InvalidInventoryQuantityException(InventoryErrorCode.INVALID_QUANTITY);
        }
        if (available > total) {
            throw new InvalidInventoryQuantityException(InventoryErrorCode.AVAILABLE_EXCEEDS_TOTAL);
        }
        return new InventoryQuantity(total, available);
    }

    static InventoryQuantity fromPersistence(int total, int available) {
        return new InventoryQuantity(total, available);
    }
}
