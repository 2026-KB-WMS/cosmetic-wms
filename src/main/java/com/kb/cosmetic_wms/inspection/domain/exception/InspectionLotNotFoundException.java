package com.kb.cosmetic_wms.inspection.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InspectionLotNotFoundException extends BusinessException {
    public InspectionLotNotFoundException() {
        super(InspectionErrorCode.INSPECTION_LOT_NOT_FOUND);
    }
}
