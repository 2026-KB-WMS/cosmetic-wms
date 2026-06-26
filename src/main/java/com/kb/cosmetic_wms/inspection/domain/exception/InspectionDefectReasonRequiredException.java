package com.kb.cosmetic_wms.inspection.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InspectionDefectReasonRequiredException extends BusinessException {
    public InspectionDefectReasonRequiredException() {
        super(InspectionErrorCode.INSPECTION_DEFECT_REASON_REQUIRED);
    }
}