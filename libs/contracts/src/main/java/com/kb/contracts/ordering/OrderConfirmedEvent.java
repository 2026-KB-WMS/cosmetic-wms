package com.kb.contracts.ordering;

import java.util.List;

public record OrderConfirmedEvent(
        Long orderId,
        Long storeId,
        List<ItemSnapshot> items
) {
    public record ItemSnapshot(Long orderItemId, Long productId, int quantity) {
    }
}