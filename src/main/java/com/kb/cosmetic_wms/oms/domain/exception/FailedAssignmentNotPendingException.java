package com.kb.cosmetic_wms.oms.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class FailedAssignmentNotPendingException extends BusinessException {
    public FailedAssignmentNotPendingException() {
        super(OmsErrorCode.FAILED_ASSIGNMENT_NOT_PENDING);
    }
}
