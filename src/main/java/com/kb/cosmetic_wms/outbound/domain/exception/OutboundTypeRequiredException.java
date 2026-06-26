package com.kb.cosmetic_wms.outbound.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class OutboundTypeRequiredException extends BusinessException {
    public OutboundTypeRequiredException() {
        super(OutboundErrorCode.OUTBOUND_TYPE_REQUIRED);
    }
}