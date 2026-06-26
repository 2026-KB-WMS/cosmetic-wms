package com.kb.cosmetic_wms.order.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class OrderItemQuantityInvalidException extends BusinessException {
    public OrderItemQuantityInvalidException() {
        super(OrderErrorCode.ORDER_ITEM_QUANTITY_INVALID);
    }
}