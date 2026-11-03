package com.kb.cosmetic_wms.oms.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class FailedAssignmentNotFoundException extends BusinessException {
    public FailedAssignmentNotFoundException() {
        super(OmsErrorCode.FAILED_ASSIGNMENT_NOT_FOUND);
    }
}
