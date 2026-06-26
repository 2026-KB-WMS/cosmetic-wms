package com.kb.cosmetic_wms.outbound.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class OutboundItemNotFoundException extends BusinessException {
    public OutboundItemNotFoundException(Long inventoryId) {
        super(OutboundErrorCode.OUTBOUND_ITEM_NOT_FOUND,
                OutboundErrorCode.OUTBOUND_ITEM_NOT_FOUND.getMessage() + ": " + inventoryId);
    }
}