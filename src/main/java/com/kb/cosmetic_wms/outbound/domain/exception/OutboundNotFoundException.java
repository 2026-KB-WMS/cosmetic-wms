package com.kb.cosmetic_wms.outbound.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class OutboundNotFoundException extends BusinessException {
    public OutboundNotFoundException() {
        super(OutboundErrorCode.OUTBOUND_NOT_FOUND);
    }
}