package com.kb.ordering.order.domain.exception;

import com.kb.common.error.BusinessException;

public class OrderStoreRequiredException extends BusinessException {
    public OrderStoreRequiredException() {
        super(OrderErrorCode.ORDER_STORE_REQUIRED);
    }
}