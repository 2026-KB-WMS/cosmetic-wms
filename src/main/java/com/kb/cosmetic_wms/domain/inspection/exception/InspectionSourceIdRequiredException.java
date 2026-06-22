package com.kb.cosmetic_wms.domain.inspection.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InspectionSourceIdRequiredException extends BusinessException {
    public InspectionSourceIdRequiredException() {
        super(InspectionErrorCode.INSPECTION_SOURCE_ID_REQUIRED);
    }
}
