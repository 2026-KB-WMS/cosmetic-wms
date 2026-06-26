package com.kb.cosmetic_wms.outbound.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class OutboundInvalidPickedQuantityException extends BusinessException {
    public OutboundInvalidPickedQuantityException() {
        super(OutboundErrorCode.OUTBOUND_INVALID_PICKED_QUANTITY);
    }
}