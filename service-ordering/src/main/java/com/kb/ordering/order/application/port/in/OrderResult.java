package com.kb.ordering.order.application.port.in;

import com.kb.ordering.order.domain.enums.OrderStatus;
import com.kb.ordering.order.domain.model.Order;

import java.util.List;

public record OrderResult(
        Long id,
        Long storeId,
        Long warehouseId,
        OrderStatus orderStatus,
        List<OrderItemResult> items
) {
    public record OrderItemResult(Long id, Long productId, int quantity) {
    }

    public static OrderResult from(Order order) {
        return new OrderResult(
                order.getId(),
                order.getStoreId(),
                order.getWarehouseId(),
                order.getOrderStatus(),
                order.getOrderItems().stream()
                        .map(item -> new OrderItemResult(item.getId(), item.getProductId(), item.getQuantity()))
                        .toList()
        );
    }
}