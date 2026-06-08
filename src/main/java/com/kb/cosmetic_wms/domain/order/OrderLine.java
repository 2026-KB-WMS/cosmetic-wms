package com.kb.cosmetic_wms.domain.order;

import com.kb.cosmetic_wms.domain.order.constants.OrderConstants;

public record OrderLine(Long productId, int quantity) {
    public OrderLine {
        if (productId == null) {
            throw new IllegalArgumentException(OrderConstants.INBOUND_PRODUCT_REQUIRED_MESSAGE);
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException(OrderConstants.INVALID_INBOUND_QUANTITY_MESSAGE);
        }
    }
}
