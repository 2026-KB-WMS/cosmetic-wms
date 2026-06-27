package com.kb.cosmetic_wms.inbound.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InboundPutawaySectionRequiredException extends BusinessException {

    public InboundPutawaySectionRequiredException() {
        super(InboundErrorCode.INBOUND_PUTAWAY_SECTION_REQUIRED);
    }
}