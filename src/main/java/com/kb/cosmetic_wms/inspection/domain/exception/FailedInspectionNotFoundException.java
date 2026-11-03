package com.kb.cosmetic_wms.inspection.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class FailedInspectionNotFoundException extends BusinessException {
    public FailedInspectionNotFoundException() {
        super(InspectionErrorCode.FAILED_INSPECTION_NOT_FOUND);
    }
}
