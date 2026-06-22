package com.kb.cosmetic_wms.domain.order.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class OrderStoreRequiredException extends BusinessException {
    public OrderStoreRequiredException() {
        super(OrderErrorCode.ORDER_STORE_REQUIRED);
    }
}