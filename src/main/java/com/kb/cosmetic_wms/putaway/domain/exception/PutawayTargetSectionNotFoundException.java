package com.kb.cosmetic_wms.putaway.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class PutawayTargetSectionNotFoundException extends BusinessException {
    public PutawayTargetSectionNotFoundException() {
        super(PutawayErrorCode.TARGET_SECTION_NOT_FOUND);
    }
}
