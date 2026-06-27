package com.kb.cosmetic_wms.inbound.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InboundInvalidAddItemStatusException extends BusinessException {

    public InboundInvalidAddItemStatusException() {
        super(InboundErrorCode.INBOUND_INVALID_ADD_ITEM_STATUS);
    }
}