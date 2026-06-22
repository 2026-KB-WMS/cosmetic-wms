package com.kb.cosmetic_wms.domain.order;

import com.kb.cosmetic_wms.domain.order.exception.OrderItemProductRequiredException;
import com.kb.cosmetic_wms.domain.order.exception.OrderItemQuantityInvalidException;

public record OrderLine(Long productId, int quantity) {
    public OrderLine {
        if (productId == null) throw new OrderItemProductRequiredException();
        if (quantity <= 0) throw new OrderItemQuantityInvalidException();
    }
}