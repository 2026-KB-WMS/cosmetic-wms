package com.kb.cosmetic_wms.order.domain.event;

import java.util.List;

public record OrderConfirmedEvent(
        Long orderId,
        Long warehouseId,
        List<ItemSnapshot> items
) {
    public record ItemSnapshot(Long orderItemId, Long productId, int quantity) {}
}
