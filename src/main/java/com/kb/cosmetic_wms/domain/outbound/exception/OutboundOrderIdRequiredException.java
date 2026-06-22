package com.kb.cosmetic_wms.domain.outbound.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class OutboundOrderIdRequiredException extends BusinessException {
    public OutboundOrderIdRequiredException() {
        super(OutboundErrorCode.OUTBOUND_ORDER_ID_REQUIRED);
    }
}