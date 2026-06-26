package com.kb.cosmetic_wms.outbound.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class OutboundCancelNotAllowedException extends BusinessException {
    public OutboundCancelNotAllowedException() {
        super(OutboundErrorCode.OUTBOUND_CANCEL_NOT_ALLOWED);
    }
}