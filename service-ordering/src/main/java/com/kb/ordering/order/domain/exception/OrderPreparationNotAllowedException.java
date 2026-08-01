package com.kb.ordering.order.domain.exception;

import com.kb.common.error.BusinessException;

public class OrderPreparationNotAllowedException extends BusinessException {
    public OrderPreparationNotAllowedException() {
        super(OrderErrorCode.ORDER_PREPARATION_NOT_ALLOWED);
    }
}