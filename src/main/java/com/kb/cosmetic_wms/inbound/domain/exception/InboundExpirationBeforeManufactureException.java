package com.kb.cosmetic_wms.inbound.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InboundExpirationBeforeManufactureException extends BusinessException {

    public InboundExpirationBeforeManufactureException() {
        super(InboundErrorCode.INBOUND_EXPIRATION_BEFORE_MANUFACTURE);
    }
}