package com.kb.cosmetic_wms.inbound.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InboundDateRequiredException extends BusinessException {

    public InboundDateRequiredException() {
        super(InboundErrorCode.INBOUND_DATE_REQUIRED);
    }
}