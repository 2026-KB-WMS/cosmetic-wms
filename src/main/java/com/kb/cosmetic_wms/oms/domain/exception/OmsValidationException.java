package com.kb.cosmetic_wms.oms.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class OmsValidationException extends BusinessException {

    public OmsValidationException(OmsErrorCode errorCode) {
        super(errorCode);
    }
}
