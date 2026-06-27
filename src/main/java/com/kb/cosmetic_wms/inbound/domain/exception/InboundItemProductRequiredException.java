package com.kb.cosmetic_wms.inbound.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InboundItemProductRequiredException extends BusinessException {

    public InboundItemProductRequiredException() {
        super(InboundErrorCode.INBOUND_ITEM_PRODUCT_REQUIRED);
    }
}