package com.kb.cosmetic_wms.domain.order.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class OrderShipNotAllowedException extends BusinessException {
    public OrderShipNotAllowedException() {
        super(OrderErrorCode.ORDER_SHIP_NOT_ALLOWED);
    }
}