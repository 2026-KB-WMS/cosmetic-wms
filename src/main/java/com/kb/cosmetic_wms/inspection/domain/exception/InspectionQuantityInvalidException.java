package com.kb.cosmetic_wms.inspection.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InspectionQuantityInvalidException extends BusinessException {
    public InspectionQuantityInvalidException() {
        super(InspectionErrorCode.INSPECTION_QUANTITY_INVALID);
    }
}