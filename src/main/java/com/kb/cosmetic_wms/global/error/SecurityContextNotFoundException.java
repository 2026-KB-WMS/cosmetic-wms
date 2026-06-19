package com.kb.cosmetic_wms.global.error;

public class SecurityContextNotFoundException extends BusinessException {

    public SecurityContextNotFoundException() {
        super(GlobalErrorCode.SECURITY_CONTEXT_NOT_FOUND);
    }
}
