package com.kb.ordering.order.domain.exception;

import com.kb.common.error.BusinessException;

public class OrderConfirmNotAllowedException extends BusinessException {
    public OrderConfirmNotAllowedException() {
        super(OrderErrorCode.ORDER_CONFIRM_NOT_ALLOWED);
    }
}