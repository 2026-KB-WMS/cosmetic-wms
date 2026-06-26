package com.kb.cosmetic_wms.outbound.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class OutboundIncompletePickingException extends BusinessException {
    public OutboundIncompletePickingException() {
        super(OutboundErrorCode.OUTBOUND_INCOMPLETE_PICKING);
    }
}