package com.kb.cosmetic_wms.inbound.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InboundLineNotFoundException extends BusinessException {

    public InboundLineNotFoundException() {
        super(InboundErrorCode.INBOUND_LINE_NOT_FOUND);
    }
}
