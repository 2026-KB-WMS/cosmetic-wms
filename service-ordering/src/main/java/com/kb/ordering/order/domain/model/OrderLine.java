package com.kb.ordering.order.domain.model;

import com.kb.ordering.order.domain.exception.OrderItemProductRequiredException;
import com.kb.ordering.order.domain.exception.OrderItemQuantityInvalidException;

public record OrderLine(Long productId, int quantity) {
    public OrderLine {
        if (productId == null) throw new OrderItemProductRequiredException();
        if (quantity <= 0) throw new OrderItemQuantityInvalidException();
    }
}