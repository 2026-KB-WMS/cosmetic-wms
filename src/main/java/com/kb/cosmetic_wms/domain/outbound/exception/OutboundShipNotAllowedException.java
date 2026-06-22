package com.kb.cosmetic_wms.domain.outbound.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class OutboundShipNotAllowedException extends BusinessException {
    public OutboundShipNotAllowedException() {
        super(OutboundErrorCode.OUTBOUND_SHIP_NOT_ALLOWED);
    }
}