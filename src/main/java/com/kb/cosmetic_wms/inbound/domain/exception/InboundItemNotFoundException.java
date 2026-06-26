package com.kb.cosmetic_wms.inbound.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InboundItemNotFoundException extends BusinessException {

    public InboundItemNotFoundException() {
        super(InboundErrorCode.INBOUND_ITEM_NOT_FOUND);
    }
}
