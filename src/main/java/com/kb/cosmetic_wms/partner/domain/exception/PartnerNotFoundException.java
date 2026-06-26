package com.kb.cosmetic_wms.partner.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class PartnerNotFoundException extends BusinessException {
    public PartnerNotFoundException() {
        super(PartnerErrorCode.PARTNER_NOT_FOUND);
    }
}