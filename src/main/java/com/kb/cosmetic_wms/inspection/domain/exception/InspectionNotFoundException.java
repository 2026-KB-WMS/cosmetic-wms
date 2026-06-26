package com.kb.cosmetic_wms.inspection.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InspectionNotFoundException extends BusinessException {
    public InspectionNotFoundException() {
        super(InspectionErrorCode.INSPECTION_NOT_FOUND);
    }
}