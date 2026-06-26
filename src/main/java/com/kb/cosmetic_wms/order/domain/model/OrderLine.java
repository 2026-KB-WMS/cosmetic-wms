package com.kb.cosmetic_wms.order.domain.model;

import com.kb.cosmetic_wms.order.domain.exception.OrderItemProductRequiredException;
import com.kb.cosmetic_wms.order.domain.exception.OrderItemQuantityInvalidException;

public record OrderLine(Long productId, int quantity) {
    public OrderLine {
        if (productId == null) throw new OrderItemProductRequiredException();
        if (quantity <= 0) throw new OrderItemQuantityInvalidException();
    }
}