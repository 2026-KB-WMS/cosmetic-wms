package com.kb.cosmetic_wms.inbound.application.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;
import com.kb.cosmetic_wms.inbound.domain.exception.InboundErrorCode;

public class InboundPartnerNotFoundException extends BusinessException {

    public InboundPartnerNotFoundException() {
        super(InboundErrorCode.INBOUND_PARTNER_NOT_FOUND);
    }
}