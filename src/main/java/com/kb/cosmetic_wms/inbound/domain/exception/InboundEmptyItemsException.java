package com.kb.cosmetic_wms.inbound.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InboundEmptyItemsException extends BusinessException {

    public InboundEmptyItemsException() {
        super(InboundErrorCode.INBOUND_EMPTY_ITEMS);
    }
}
