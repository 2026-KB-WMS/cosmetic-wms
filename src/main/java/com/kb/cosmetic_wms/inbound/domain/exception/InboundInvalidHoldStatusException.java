package com.kb.cosmetic_wms.inbound.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InboundInvalidHoldStatusException extends BusinessException {

    public InboundInvalidHoldStatusException() {
        super(InboundErrorCode.INBOUND_INVALID_HOLD_STATUS);
    }
}