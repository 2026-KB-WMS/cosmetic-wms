package com.kb.cosmetic_wms.order.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class OrderWarehouseRequiredException extends BusinessException {
    public OrderWarehouseRequiredException() {
        super(OrderErrorCode.ORDER_WAREHOUSE_REQUIRED);
    }
}