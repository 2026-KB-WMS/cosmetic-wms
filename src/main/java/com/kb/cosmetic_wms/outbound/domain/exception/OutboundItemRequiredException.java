package com.kb.cosmetic_wms.outbound.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class OutboundItemRequiredException extends BusinessException {
    public OutboundItemRequiredException() {
        super(OutboundErrorCode.OUTBOUND_ITEM_REQUIRED);
    }
}