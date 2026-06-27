package com.kb.cosmetic_wms.inbound.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InboundExpirationDateRequiredException extends BusinessException {

    public InboundExpirationDateRequiredException() {
        super(InboundErrorCode.INBOUND_EXPIRATION_DATE_REQUIRED);
    }
}