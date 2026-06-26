package com.kb.cosmetic_wms.outbound.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class OutboundOrderIdRequiredException extends BusinessException {
    public OutboundOrderIdRequiredException() {
        super(OutboundErrorCode.OUTBOUND_ORDER_ID_REQUIRED);
    }
}