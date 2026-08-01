package com.kb.ordering.order.domain.exception;

import com.kb.common.error.BusinessException;

public class OrderNotFoundException extends BusinessException {
    public OrderNotFoundException() {
        super(OrderErrorCode.ORDER_NOT_FOUND);
    }
}