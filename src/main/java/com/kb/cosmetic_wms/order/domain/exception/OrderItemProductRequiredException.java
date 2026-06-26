package com.kb.cosmetic_wms.order.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class OrderItemProductRequiredException extends BusinessException {
    public OrderItemProductRequiredException() {
        super(OrderErrorCode.ORDER_ITEM_PRODUCT_REQUIRED);
    }
}