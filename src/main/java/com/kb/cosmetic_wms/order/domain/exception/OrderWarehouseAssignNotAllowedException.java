package com.kb.cosmetic_wms.order.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class OrderWarehouseAssignNotAllowedException extends BusinessException {

    public OrderWarehouseAssignNotAllowedException() {
        super(OrderErrorCode.ORDER_WAREHOUSE_ASSIGN_NOT_ALLOWED);
    }
}
