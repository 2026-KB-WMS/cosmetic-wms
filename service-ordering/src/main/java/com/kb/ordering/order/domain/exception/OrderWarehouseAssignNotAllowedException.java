package com.kb.ordering.order.domain.exception;

import com.kb.common.error.BusinessException;

public class OrderWarehouseAssignNotAllowedException extends BusinessException {

    public OrderWarehouseAssignNotAllowedException() {
        super(OrderErrorCode.ORDER_WAREHOUSE_ASSIGN_NOT_ALLOWED);
    }
}
