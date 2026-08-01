package com.kb.ordering.order.domain.exception;

import com.kb.common.error.BusinessException;

public class OrderShipNotAllowedException extends BusinessException {
    public OrderShipNotAllowedException() {
        super(OrderErrorCode.ORDER_SHIP_NOT_ALLOWED);
    }
}