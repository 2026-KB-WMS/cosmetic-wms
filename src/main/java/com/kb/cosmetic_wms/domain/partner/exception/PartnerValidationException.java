package com.kb.cosmetic_wms.domain.partner.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class PartnerValidationException extends BusinessException {
    public PartnerValidationException() {
        super(PartnerErrorCode.PARTNER_NOT_FOUND);
    }
}
