package com.kb.cosmetic_wms.inspection.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InspectionQuantityMismatchException extends BusinessException {
    public InspectionQuantityMismatchException(int inspectionQuantity) {
        super(InspectionErrorCode.INSPECTION_QUANTITY_MISMATCH,
              "합격 수량과 반려 수량의 합이 총 검사 수량(" + inspectionQuantity + ")과 일치해야 합니다.");
    }
}