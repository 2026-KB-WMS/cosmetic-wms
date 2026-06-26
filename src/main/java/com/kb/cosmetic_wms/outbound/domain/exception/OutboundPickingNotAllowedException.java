package com.kb.cosmetic_wms.outbound.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class OutboundPickingNotAllowedException extends BusinessException {
    public OutboundPickingNotAllowedException() {
        super(OutboundErrorCode.OUTBOUND_PICKING_NOT_ALLOWED);
    }
}