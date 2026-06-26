package com.kb.cosmetic_wms.outbound.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class OutboundProcessingNotAllowedException extends BusinessException {
    public OutboundProcessingNotAllowedException() {
        super(OutboundErrorCode.OUTBOUND_PROCESSING_NOT_ALLOWED);
    }
}