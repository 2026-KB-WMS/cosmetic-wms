package com.kb.cosmetic_wms.partner.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class PartnerValidationException extends BusinessException {
    public PartnerValidationException() {
        super(PartnerErrorCode.PARTNER_VALIDATION_FAILED);
    }
}