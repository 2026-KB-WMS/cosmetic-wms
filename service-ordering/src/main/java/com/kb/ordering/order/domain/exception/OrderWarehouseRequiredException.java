package com.kb.ordering.order.domain.exception;

import com.kb.common.error.BusinessException;

public class OrderWarehouseRequiredException extends BusinessException {
    public OrderWarehouseRequiredException() {
        super(OrderErrorCode.ORDER_WAREHOUSE_REQUIRED);
    }
}