package com.kb.cosmetic_wms.domain.order.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class OrderItemProductRequiredException extends BusinessException {
    public OrderItemProductRequiredException() {
        super(OrderErrorCode.ORDER_ITEM_PRODUCT_REQUIRED);
    }
}