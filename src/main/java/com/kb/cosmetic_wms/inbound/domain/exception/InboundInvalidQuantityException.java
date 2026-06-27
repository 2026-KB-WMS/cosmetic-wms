package com.kb.cosmetic_wms.inbound.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InboundInvalidQuantityException extends BusinessException {

    public InboundInvalidQuantityException() {
        super(InboundErrorCode.INBOUND_ITEM_INVALID_QUANTITY);
    }
}