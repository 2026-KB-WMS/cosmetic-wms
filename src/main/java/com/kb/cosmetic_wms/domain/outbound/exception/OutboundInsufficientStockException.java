package com.kb.cosmetic_wms.domain.outbound.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class OutboundInsufficientStockException extends BusinessException {
    public OutboundInsufficientStockException() {
        super(OutboundErrorCode.OUTBOUND_INSUFFICIENT_STOCK);
    }
}