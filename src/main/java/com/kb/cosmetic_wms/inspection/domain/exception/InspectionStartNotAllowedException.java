package com.kb.cosmetic_wms.inspection.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InspectionStartNotAllowedException extends BusinessException {
    public InspectionStartNotAllowedException() {
        super(InspectionErrorCode.INSPECTION_START_NOT_ALLOWED);
    }
}