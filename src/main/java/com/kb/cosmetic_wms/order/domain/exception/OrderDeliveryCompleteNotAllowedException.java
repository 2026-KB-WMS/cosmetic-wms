package com.kb.cosmetic_wms.order.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class OrderDeliveryCompleteNotAllowedException extends BusinessException {
    public OrderDeliveryCompleteNotAllowedException() {
        super(OrderErrorCode.ORDER_DELIVERY_COMPLETE_NOT_ALLOWED);
    }
}