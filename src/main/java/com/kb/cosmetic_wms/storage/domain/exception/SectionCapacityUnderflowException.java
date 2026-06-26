package com.kb.cosmetic_wms.storage.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class SectionCapacityUnderflowException extends BusinessException {

    public SectionCapacityUnderflowException() {
        super(StorageErrorCode.SECTION_CAPACITY_UNDERFLOW);
    }
}