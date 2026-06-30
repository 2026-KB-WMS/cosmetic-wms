package com.kb.cosmetic_wms.putaway.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class PutawayAlreadyCompletedException extends BusinessException {
    public PutawayAlreadyCompletedException() {
        super(PutawayErrorCode.ALREADY_COMPLETED);
    }
}
