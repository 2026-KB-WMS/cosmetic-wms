package com.kb.cosmetic_wms.inspection.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InspectionSourceTypeRequiredException extends BusinessException {
    public InspectionSourceTypeRequiredException() {
        super(InspectionErrorCode.INSPECTION_SOURCE_TYPE_REQUIRED);
    }
}