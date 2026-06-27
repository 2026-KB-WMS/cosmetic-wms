package com.kb.cosmetic_wms.inbound.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InboundPutawayLotRequiredException extends BusinessException {

    public InboundPutawayLotRequiredException() {
        super(InboundErrorCode.INBOUND_PUTAWAY_LOT_REQUIRED);
    }
}