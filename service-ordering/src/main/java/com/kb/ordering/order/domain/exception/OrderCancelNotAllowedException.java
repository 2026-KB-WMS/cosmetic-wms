package com.kb.ordering.order.domain.exception;

import com.kb.common.error.BusinessException;

public class OrderCancelNotAllowedException extends BusinessException {
    public OrderCancelNotAllowedException() {
        super(OrderErrorCode.ORDER_CANCEL_NOT_ALLOWED);
    }
}