package com.kb.cosmetic_wms.order.application.port.in;

import com.kb.cosmetic_wms.order.domain.model.Order;

import java.util.List;

public record OrderAssignmentTarget(
        Long orderId,
        Long storeId,
        boolean awaitingAssignment,
        List<Item> items
) {
    public record Item(Long orderItemId, Long productId, int quantity) {
    }

    public static OrderAssignmentTarget from(Order order) {
        return new OrderAssignmentTarget(
                order.getId(),
                order.getStoreId(),
                order.isAwaitingWarehouseAssignment(),
                order.getOrderItems().stream()
                        .map(item -> new Item(item.getId(), item.getProductId(), item.getQuantity()))
                        .toList()
        );
    }
}
