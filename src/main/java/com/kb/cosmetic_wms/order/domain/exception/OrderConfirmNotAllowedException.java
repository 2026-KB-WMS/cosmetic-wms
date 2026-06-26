package com.kb.cosmetic_wms.order.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class OrderConfirmNotAllowedException extends BusinessException {
    public OrderConfirmNotAllowedException() {
        super(OrderErrorCode.ORDER_CONFIRM_NOT_ALLOWED);
    }
}