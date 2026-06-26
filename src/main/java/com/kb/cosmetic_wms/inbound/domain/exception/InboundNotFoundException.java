package com.kb.cosmetic_wms.inbound.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InboundNotFoundException extends BusinessException {

    public InboundNotFoundException() {
        super(InboundErrorCode.INBOUND_NOT_FOUND);
    }
}
