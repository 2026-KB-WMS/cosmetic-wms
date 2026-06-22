package com.kb.cosmetic_wms.domain.order.event;

import com.kb.cosmetic_wms.domain.order.entity.Orders;

import java.util.List;

public record OrderConfirmedEvent(
        Long orderId,
        List<ItemSnapshot> items
) {

    public record ItemSnapshot(
            Long productId,
            int quantity
    ) {
    }

    public static OrderConfirmedEvent from(Orders order) {
        List<ItemSnapshot> snapshots = order.getOrderItems().stream()
                .map(item -> new ItemSnapshot(item.getProductId(), item.getQuantity()))
                .toList();
        return new OrderConfirmedEvent(order.getId(), snapshots);
    }
}