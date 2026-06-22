package com.kb.cosmetic_wms.domain.inspection.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InspectionInspectorIdRequiredException extends BusinessException {
    public InspectionInspectorIdRequiredException() {
        super(InspectionErrorCode.INSPECTION_INSPECTOR_ID_REQUIRED);
    }
}
