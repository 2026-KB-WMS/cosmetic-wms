package com.kb.cosmetic_wms.inbound.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InboundLinesRequiredException extends BusinessException {

    public InboundLinesRequiredException() {
        super(InboundErrorCode.INBOUND_LINES_REQUIRED);
    }
}