package com.kb.cosmetic_wms.oms.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class AssignmentOrderNotFoundException extends BusinessException {
    public AssignmentOrderNotFoundException() {
        super(OmsErrorCode.ASSIGNMENT_ORDER_NOT_FOUND);
    }
}
