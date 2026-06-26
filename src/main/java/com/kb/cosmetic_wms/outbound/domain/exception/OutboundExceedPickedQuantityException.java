package com.kb.cosmetic_wms.outbound.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class OutboundExceedPickedQuantityException extends BusinessException {
    public OutboundExceedPickedQuantityException() {
        super(OutboundErrorCode.OUTBOUND_EXCEED_PICKED_QUANTITY);
    }
}