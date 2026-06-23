package com.kb.cosmetic_wms.global.event;

import java.util.List;

public record OrderConfirmedEvent(
        Long orderId,
        Long warehouseId,
        List<ItemSnapshot> items
) {
    public record ItemSnapshot(Long orderItemId, Long productId, int quantity) {}
}
