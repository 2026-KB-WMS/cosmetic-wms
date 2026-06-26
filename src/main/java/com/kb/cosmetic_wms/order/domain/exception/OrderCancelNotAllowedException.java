package com.kb.cosmetic_wms.order.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class OrderCancelNotAllowedException extends BusinessException {
    public OrderCancelNotAllowedException() {
        super(OrderErrorCode.ORDER_CANCEL_NOT_ALLOWED);
    }
}