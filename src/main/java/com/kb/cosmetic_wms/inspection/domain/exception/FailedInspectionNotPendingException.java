package com.kb.cosmetic_wms.inspection.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class FailedInspectionNotPendingException extends BusinessException {
    public FailedInspectionNotPendingException() {
        super(InspectionErrorCode.FAILED_INSPECTION_NOT_PENDING);
    }
}
