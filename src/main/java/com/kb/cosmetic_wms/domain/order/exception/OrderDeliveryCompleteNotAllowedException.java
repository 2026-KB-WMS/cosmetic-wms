package com.kb.cosmetic_wms.domain.order.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class OrderDeliveryCompleteNotAllowedException extends BusinessException {
    public OrderDeliveryCompleteNotAllowedException() {
        super(OrderErrorCode.ORDER_DELIVERY_COMPLETE_NOT_ALLOWED);
    }
}