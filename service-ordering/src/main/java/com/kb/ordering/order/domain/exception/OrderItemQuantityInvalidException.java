package com.kb.ordering.order.domain.exception;

import com.kb.common.error.BusinessException;

public class OrderItemQuantityInvalidException extends BusinessException {
    public OrderItemQuantityInvalidException() {
        super(OrderErrorCode.ORDER_ITEM_QUANTITY_INVALID);
    }
}