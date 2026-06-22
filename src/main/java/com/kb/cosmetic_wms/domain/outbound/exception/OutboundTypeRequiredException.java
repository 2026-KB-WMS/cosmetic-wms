package com.kb.cosmetic_wms.domain.outbound.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class OutboundTypeRequiredException extends BusinessException {
    public OutboundTypeRequiredException() {
        super(OutboundErrorCode.OUTBOUND_TYPE_REQUIRED);
    }
}