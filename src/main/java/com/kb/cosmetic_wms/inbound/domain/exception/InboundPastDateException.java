package com.kb.cosmetic_wms.inbound.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InboundPastDateException extends BusinessException {

    public InboundPastDateException() {
        super(InboundErrorCode.INBOUND_PAST_DATE);
    }
}