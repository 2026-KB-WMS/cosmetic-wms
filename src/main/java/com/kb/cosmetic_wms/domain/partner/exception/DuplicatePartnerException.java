package com.kb.cosmetic_wms.domain.partner.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class DuplicatePartnerException extends BusinessException {
    public DuplicatePartnerException() {
        super(PartnerErrorCode.DUPLICATE_PARTNER);
    }
}
