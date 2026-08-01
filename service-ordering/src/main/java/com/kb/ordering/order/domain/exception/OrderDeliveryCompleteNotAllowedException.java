package com.kb.ordering.order.domain.exception;

import com.kb.common.error.BusinessException;

public class OrderDeliveryCompleteNotAllowedException extends BusinessException {
    public OrderDeliveryCompleteNotAllowedException() {
        super(OrderErrorCode.ORDER_DELIVERY_COMPLETE_NOT_ALLOWED);
    }
}