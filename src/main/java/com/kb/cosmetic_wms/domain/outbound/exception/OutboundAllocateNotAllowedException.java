package com.kb.cosmetic_wms.domain.outbound.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class OutboundAllocateNotAllowedException extends BusinessException {
    public OutboundAllocateNotAllowedException() {
        super(OutboundErrorCode.OUTBOUND_ALLOCATE_NOT_ALLOWED);
    }
}