package com.kb.cosmetic_wms.order.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class OrderNotFoundException extends BusinessException {
    public OrderNotFoundException() {
        super(OrderErrorCode.ORDER_NOT_FOUND);
    }
}