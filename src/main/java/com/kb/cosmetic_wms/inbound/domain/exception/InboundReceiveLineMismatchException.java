package com.kb.cosmetic_wms.inbound.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InboundReceiveLineMismatchException extends BusinessException {

    public InboundReceiveLineMismatchException() {
        super(InboundErrorCode.INBOUND_RECEIVE_LINE_MISMATCH);
    }
}