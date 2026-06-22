package com.kb.cosmetic_wms.domain.inspection.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InspectionCompleteNotAllowedException extends BusinessException {
    public InspectionCompleteNotAllowedException() {
        super(InspectionErrorCode.INSPECTION_COMPLETE_NOT_ALLOWED);
    }
}
