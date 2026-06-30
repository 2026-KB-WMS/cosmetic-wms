package com.kb.cosmetic_wms.putaway.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class PutawayOrderNotFoundException extends BusinessException {
    public PutawayOrderNotFoundException() {
        super(PutawayErrorCode.PUTAWAY_ORDER_NOT_FOUND);
    }
}
