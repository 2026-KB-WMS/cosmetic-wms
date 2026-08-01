package com.kb.ordering.order.domain.exception;

import com.kb.common.error.BusinessException;

public class OrderItemProductRequiredException extends BusinessException {
    public OrderItemProductRequiredException() {
        super(OrderErrorCode.ORDER_ITEM_PRODUCT_REQUIRED);
    }
}