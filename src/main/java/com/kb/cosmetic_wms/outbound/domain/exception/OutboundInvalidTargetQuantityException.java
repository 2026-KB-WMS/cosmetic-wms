package com.kb.cosmetic_wms.outbound.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class OutboundInvalidTargetQuantityException extends BusinessException {
    public OutboundInvalidTargetQuantityException() {
        super(OutboundErrorCode.OUTBOUND_INVALID_TARGET_QUANTITY);
    }
}