package com.kb.cosmetic_wms.storage.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class DuplicateSectionCodeException extends BusinessException {

    public DuplicateSectionCodeException() {
        super(StorageErrorCode.DUPLICATE_SECTION_CODE);
    }
}