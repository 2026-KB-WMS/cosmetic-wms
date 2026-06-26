package com.kb.cosmetic_wms.outbound.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class OutboundAllocateNotAllowedException extends BusinessException {
    public OutboundAllocateNotAllowedException() {
        super(OutboundErrorCode.OUTBOUND_ALLOCATE_NOT_ALLOWED);
    }
}