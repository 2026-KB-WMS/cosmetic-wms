package com.kb.cosmetic_wms.inspection.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InspectionNegativeQuantityException extends BusinessException {
    public InspectionNegativeQuantityException() {
        super(InspectionErrorCode.INSPECTION_NEGATIVE_QUANTITY);
    }
}