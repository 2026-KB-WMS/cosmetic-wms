package com.kb.cosmetic_wms.storage.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class SectionCapacityOverflowException extends BusinessException {

    public SectionCapacityOverflowException() {
        super(StorageErrorCode.SECTION_CAPACITY_OVERFLOW);
    }
}
