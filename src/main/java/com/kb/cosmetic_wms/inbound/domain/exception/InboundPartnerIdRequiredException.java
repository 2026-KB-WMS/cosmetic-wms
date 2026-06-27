package com.kb.cosmetic_wms.inbound.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InboundPartnerIdRequiredException extends BusinessException {

    public InboundPartnerIdRequiredException() {
        super(InboundErrorCode.INBOUND_PARTNER_ID_REQUIRED);
    }
}