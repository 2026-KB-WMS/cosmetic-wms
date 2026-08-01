package com.kb.ordering.order.domain.exception;

import com.kb.common.error.BusinessException;

public class OrderItemsRequiredException extends BusinessException {
    public OrderItemsRequiredException() {
        super(OrderErrorCode.ORDER_ITEMS_REQUIRED);
    }
}