package com.kb.cosmetic_wms.order.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class OrderStoreRequiredException extends BusinessException {
    public OrderStoreRequiredException() {
        super(OrderErrorCode.ORDER_STORE_REQUIRED);
    }
}