package com.kb.cosmetic_wms.domain.order.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class OrderPreparationNotAllowedException extends BusinessException {
    public OrderPreparationNotAllowedException() {
        super(OrderErrorCode.ORDER_PREPARATION_NOT_ALLOWED);
    }
}