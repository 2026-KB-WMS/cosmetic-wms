package com.kb.cosmetic_wms.domain.order.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class OrderCancelNotAllowedException extends BusinessException {
    public OrderCancelNotAllowedException() {
        super(OrderErrorCode.ORDER_CANCEL_NOT_ALLOWED);
    }
}
