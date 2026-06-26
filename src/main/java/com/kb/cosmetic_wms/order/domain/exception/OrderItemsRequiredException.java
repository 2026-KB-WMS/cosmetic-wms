package com.kb.cosmetic_wms.order.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class OrderItemsRequiredException extends BusinessException {
    public OrderItemsRequiredException() {
        super(OrderErrorCode.ORDER_ITEMS_REQUIRED);
    }
}