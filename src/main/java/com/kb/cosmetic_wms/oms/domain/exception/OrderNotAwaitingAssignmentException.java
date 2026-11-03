package com.kb.cosmetic_wms.oms.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class OrderNotAwaitingAssignmentException extends BusinessException {
    public OrderNotAwaitingAssignmentException() {
        super(OmsErrorCode.ORDER_NOT_AWAITING_ASSIGNMENT);
    }
}
