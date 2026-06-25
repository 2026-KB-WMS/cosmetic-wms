package com.kb.cosmetic_wms.inbound.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InboundProductNotFoundException extends BusinessException {

    public InboundProductNotFoundException() {
        super(InboundErrorCode.INBOUND_PRODUCT_NOT_FOUND);
    }
}
